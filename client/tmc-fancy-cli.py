from __future__ import print_function, unicode_literals
import os
import json
import time
from typing import Any
from tabulate import tabulate
import requests
import configparser
from PyInquirer import prompt, Separator

title_art = """"   .----------------.  .----------------.  .----------------.  .----------------.  .----------------.  .----------------.  .----------------. 
   | .--------------. || .--------------. || .--------------. || .--------------. || .--------------. || .--------------. || .--------------. | 
   | |  _________   | || | ____    ____ | || |     ______   | || |              | || |     ______   | || |   _____      | || |     _____    | | 
   | | |  _   _  |  | || ||_   \  /   _|| || |   .' ___  |  | || |              | || |   .' ___  |  | || |  |_   _|     | || |    |_   _|   | | 
   | | |_/ | | \_|  | || |  |   \/   |  | || |  / .'   \_|  | || |    ______    | || |  / .'   \_|  | || |    | |       | || |      | |     | | 
   | |     | |      | || |  | |\  /| |  | || |  | |         | || |   |______|   | || |  | |         | || |    | |   _   | || |      | |     | | 
   | |    _| |_     | || | _| |_\/_| |_ | || |  \ `.___.'\  | || |              | || |  \ `.___.'\  | || |   _| |__/ |  | || |     _| |_    | | 
   | |   |_____|    | || ||_____||_____|| || |   `._____.'  | || |              | || |   `._____.'  | || |  |________|  | || |    |_____|   | | 
   | |              | || |              | || |              | || |              | || |              | || |              | || |              | | 
   | '--------------' || '--------------' || '--------------' || '--------------' || '--------------' || '--------------' || '--------------' | 
    '----------------'  '----------------'  '----------------'  '----------------'  '----------------'  '----------------'  '----------------'  """""

config = configparser.ConfigParser()
config.read('config.ini')

# Global scope
intersections = [1, 2, 3]
minikube_ip = 0
intersection_port = 0
meta_service_port = 0
user_role = None
intersection_dict = {}

with open('menus.json') as menus:
    interactive_menus = json.load(menus)


def clear_console():
    os.system('cls' if os.name == 'nt' else 'clear')


def state_color(state) -> str:
    if state == "GREEN":
        return "\033[92m GREEN \033[0m"
    elif state == "RED":
        return "\033[91m RED   \033[0m"
    elif state == "YELLOW":
        return "\033[93m YELLOW \033[0m"
    elif state == "RED_YELLOW":
        return "\033[91m RED_ \033[93m YELLOW \033[0m"
    elif state == "EMERGENCY":
        return "\033[43m EMERGENCY \033[0m"


def traffic_light_to_string(light, type, location) -> str:
    res = [light["id"]["uuid"], state_color(light["state"])]
    if type:
        res.append(" \033[1m" + light["type"] + "\033[0m")
    if location:
        res.append(" \033[2m" + str(light["id"]["location"]["latitude"]) + "\033[0m")
        res.append(" \033[2m" + str(light["id"]["location"]["longitude"]) + "\033[0m")
    return res


def uuid_picker():
    global intersection_dict
    question = interactive_menus["ids_template"]
    for k in intersection_dict.keys():
        question[0]["choices"].append(Separator("<<Intersection " + str(k) + ">>"))
        for item in intersection_dict[k]:
            question[0]["choices"].append(item)
    uuid = prompt(question)
    for k in intersection_dict.keys():
        if uuid["id"] in intersection_dict[k]:
            return k, uuid["id"]


def intersection_picker():
    global intersection_dict
    question = interactive_menus["intersections_menu"]
    question[0]["choices"] = []
    for k in intersection_dict.keys():
        question[0]["choices"].append(str(k))
    intersection = prompt(question)
    return int(intersection["intersectionNr"])


def fetch_access_token_with_password(username: str, password: str, client_id: str = "green-light-request-client", local: bool = False):
    if local:
        url = f"https://localhost:31141/auth/realms/Duckburg-Traffic-Control/protocol/openid-connect/token"
        # url = f"http://localhost:31142/auth/realms/Duckburg-Traffic-Control/protocol/openid-connect/token"
    else:
        print("Requesting over minikube (http) ")
        url = f"http://{minikube_ip}:31142/auth/realms/Duckburg-Traffic-Control/protocol/openid-connect/token"
    payload = {
        "client_id": client_id,
        "grant_type": "password",
        "username": username,
        "password": password
    }
    r = requests.post(url, data=payload, verify="truststore.pem")
    if r.status_code != 200:
        print(f"Something went wrong (status code: {r.status_code}")
        print(r.request.headers)
        print(r.request.body)
        return ""
    return r.json()["access_token"]


def log_in(username: str, password: str, local: bool = False):
    access_token = fetch_access_token_with_password(username, password, local = local)
    if access_token == "":
        print("Something went wrong. Please try again.")
        exit()
    print(" ")
    print(f"-----  Access-Token for {username} ------")
    print(access_token)
    print(f"------------------------------------------")
    return access_token


def login_emergency_vehicle(local: bool = False):
    username = config['DefaultUsers']['EmergencyVehicleDefaultName']
    password = config['DefaultUsers']['EmergencyVehicleDefaultPW']
    return log_in(username, password, local)


def login_mayor(local: bool = False):
    username = config['DefaultUsers']['MayorDefaultName']
    password = config['DefaultUsers']['MayorDefaultPW']
    return log_in(username, password, local)


def login_autonomous_vehicle(local: bool = False):
    username = config['DefaultUsers']['AutonomousVehicleDefaultName']
    password = config['DefaultUsers']['AutonomousVehicleDefaultPW']
    return log_in(username, password, local)


def login_tmc(local: bool = False):
    username = config['DefaultUsers']['TMC']
    password = config['DefaultUsers']['TMC_pw']
    return log_in(username, password, local)


def query_traffic_lights_option(intersection_number: int):
    url = f"https://{minikube_ip}:{meta_service_port}/meta/intersection/{intersection_number}/trafficLights"
    r = requests.get(url, verify="truststore.pem")
    if r.status_code != 200:
        print(f"Something went wrong (status code: {r.status_code} - url: {r.url}")
        return {}
    data = r.json()
    tl_table = []
    for item in data:
        tl_table.append(traffic_light_to_string(item, True, True))
    print(tabulate(tl_table, ["ID", "State", "Type", "Latitude", "Longitude"], tablefmt="grid"))


def authorized_get_request(url: str, access_token: str):
    headers = {
        "Authorization": f"Bearer {access_token}"
    }
    resp = requests.get(url, headers=headers, verify="truststore.pem")
    if resp.status_code == 409:
        print("\033[31mFailed. Probably intersection is blocked due to another request.\033[0m")
        return None
    elif resp.status_code == 400:
        print("\033[31mFailed. Your action is not accepted by server.\033[0m")
        return None
    elif resp.status_code != 200: #Unknwon errors
        print(f"\033[31mSomething went wrong I am afraid.\033[0m \n Debug info:")
        print(f"status code: {resp.status_code}")
        print(f"body: {resp.text}")
        print(f"request: {resp.request.headers}")
        return None
    return resp


def authorized_post_request(url: str, payload: Any, access_token: str):
    headers = {
        "Authorization": f"Bearer {access_token}"
    }
    resp = requests.post(url, headers=headers, json=payload, verify="truststore.pem")
    if resp is None:
        print("\033[31mFailed. Did not even get a response from the server. Big Sad:(\033[0m")
        return None
    elif resp.status_code == 409:
        print("\033[31mFailed. Probably intersection is blocked due to another request.\033[0m")
        return None
    elif resp.status_code == 400:
        print("\033[31mFailed. Your action is not accepted by server.\033[0m")
        return None
    elif resp.status_code != 200:
        print(f"\033[31mSomething went wrong I am afraid.\033[0m \n Debug info:")
        print(f"status code: {resp.status_code}")
        print(f"body: {resp.text}")
        print(f"request: {resp.request.headers}")
        return None
    return resp


def request_green_light_option(access_token: str):
    intersection_choice, traffic_light_id = uuid_picker()
    request_green_light_url = f"https://{minikube_ip}:{meta_service_port}/meta/intersection/{intersection_choice}/requestGreen/{traffic_light_id}"
    resp = authorized_get_request(request_green_light_url, access_token)
    if resp is not None:
        print("\033[32mRequest granted :)\033[0m")
        print("The traffic light will now transition to green and is blocked until:")
        print(f">> {resp.text}")


def set_state_at_intersection_option(access_token: str, local = False):
    intersection_nr = intersection_picker()
    url_post = f"https://{minikube_ip}:{meta_service_port}/meta/intersection/{intersection_nr}/requestState"
    url = f"https://{minikube_ip}:{meta_service_port}/meta/intersection/{intersection_nr}/trafficLights"
    r = requests.get(url, verify="truststore.pem")
    lights = r.json()
    question = []
    template = interactive_menus["chose_color_template"][0]
    for item in lights:
        template = interactive_menus["chose_color_template"][0].copy()
        template["name"] = item["id"]["uuid"]
        template["message"] = f"{item['id']['uuid']} ({str(item['type'])[:1]})  NOW: {item['state']}"
        template["default"] = state_to_option_mapping(item["state"])
        question.append(template)
    print("Change the state with G-R-Y-W-E")
    print("Press H for help")
    ans = prompt(question)
    req = {}
    req["states"] = ans
    req["issuer"] = "00000000-0000-0000-0000-000000000000"
    req["timestamp"] = "2022-01-01T00:00:00.0"
    req["minDuration"] = 5

    resp = authorized_post_request(url_post, payload=req, access_token=access_token)
    if resp is not None:
        print("\033[32mSuccess :)\033[0m")


def set_green_wave_option(access_token : str):
    url = f"https://{minikube_ip}:{meta_service_port}/meta/greenWaveEW"
    resp = authorized_get_request(url, access_token)
    if resp is None:
        return
    if resp.status_code == 200:
        print("\033[32mSuccess :)\033[0m")
    else:
        print("Failure :(")
        print(f"status code : {resp.status_code}")



def state_to_option_mapping(state: str) -> str:
    if state == "GREEN":
        return 'g'
    elif state == "RED":
        return 'r'
    elif state == "YELLOW":
        return 'y'
    elif state == "RED_YELLOW":
        return 'w'
    elif state == "EMERGENCY":
        return 'e'


def query_specific_traffic_light_state_option(intersection_nr, traffic_light_uuid):
    query_traffic_light_state_url = f"https://{minikube_ip}:{meta_service_port}/meta/intersection/{intersection_nr}/trafficLights/{traffic_light_uuid}"
    resp = requests.get(query_traffic_light_state_url, verify="truststore.pem")
    if resp.status_code != 200:
        print(f"\033[31mSomething went wrong I am afraid.\033[0m \nDebug info:")
        print(f"status code: {resp.status_code}")
        print(f"body: {resp.text}")
        print(f"request: {resp.request.headers}")
        exit()
    traffic_light_json = resp.json()
    print(tabulate([traffic_light_to_string(traffic_light_json, True, True)],
                   ["ID", "State", "Type", "Latitude", "Longitude"], tablefmt="grid"))


def get_status_information_all_intersections():
    intersection_query_request_url = f"https://{minikube_ip}:{meta_service_port}/meta"
    r = requests.get(intersection_query_request_url, verify="truststore.pem")
    if r.status_code != 200:  # What do here?
        print(f"\033[31mSomething went wrong\033[0m (status code: {r.status_code} - url: {r.url}")
        return {}
    json_data_meta = r.json()
    return json_data_meta


def is_connected(json_data_meta, intersection_nr):
    status_row = json_data_meta[f"Status Intersection{intersection_nr}"]
    if status_row == "connected":
        return True
    return False


def get_intersection_info(intersection_nr):
        intersection_query_request_url = f"https://{minikube_ip}:{meta_service_port}/meta/intersection/{intersection_nr}"
        r = requests.get(intersection_query_request_url, verify="truststore.pem")
        if r.status_code != 200:  # What do here?
            print(f"\033[31mSomething went wrong\033[0m (status code: {r.status_code} - url: {r.url}")
            return {}
        json_data = r.json()
        return json_data


def tabulate_intersection_info(json_data):
        info_row = json_data["Info"]

        table = [["\033[1mInfo\033[0m", json_data["Info"]],
        ["\033[1mLatitude\033[0m", json_data["Latitude"]],
        ["\033[1mLongitude\033[0m", json_data["Longitude"]],
        ["\033[1mTraffic Lights\033[0m", json_data["trafficLightsConnected"]]]

        blocked_until = json_data["blockedUntil"]
        if blocked_until is None:
            table.append(["\033[1mStatus\033[0m", "\033[32mAvailable\033[0m"])
        else:
            table.append(["\033[1mStatus\033[0m", "\033[31mBlocked until \033[0m" + str(blocked_until)])
        return table


def list_all_intersections_option():
    json_data_meta = get_status_information_all_intersections()
    for x in range(1, 4):
        table = []
        connected = is_connected(json_data_meta, x)
        if connected:
            table.append([f"\033[1mIntersection {x}\033[0m", "\033[32m Connected \033[0m"])
            json_data = get_intersection_info(x)
            table.extend(tabulate_intersection_info(json_data))
        else:
            table.append([f"\033[1mIntersection {x}\033[0m", "\033[31m Disconnected \033[0m"])

        print(tabulate(table, tablefmt="grid"))
        print("\n")


def car_animation():
    car = [
        "   d8888 888______888_888888P'  ",
        "      d8'888 888  /\  888 A 888 ",
        "    ,d8' 888 888,'  `.888/ \888 ",
        "  ,d8Y'__888 888______888___888 ",
        " 8888888888 888888888888   888  ",
        "[888P""Y888 888888888888,--888] ",
        "  88P db Y8P Y8888888888P db Y8 ",
        "    \ YP /              \ YP /  ",
        "    `--'                `--'""  "]

    space = "                             "
    for i in range(1, 65):
        print("\n\n")
        for car_line in car:
            line = space + car_line
            print(line[i:])
        time.sleep(0.03)
        clear_console()


def traffic_light_data():
    global intersection_dict
    answer = prompt(interactive_menus["general_traffic_lights_menu"])
    if answer["action"] == "All traffic lights at intersection":
        intersection = intersection_picker()
        query_traffic_lights_option(intersection)
    else:
        intersection_nr, uuid = uuid_picker()
        print(intersection_nr, uuid)
        query_specific_traffic_light_state_option(intersection_nr, uuid)


def load_ids():
    global intersection_dict
    intersection_dict = {}
    for intersection_nr in range(1, 4):
        url = f"https://{minikube_ip}:{meta_service_port}/meta/intersection/{intersection_nr}/trafficLights"
        r = requests.get(url, verify="truststore.pem")
        if r.status_code == 200:
            ids = []
            data = r.json()
            for item in data:
                ids.append(item["id"]["uuid"])
            intersection_dict[intersection_nr] = ids


def local_or_minikube():
    global minikube_ip
    global intersection_port
    global meta_service_port
    ans = prompt(interactive_menus["placement_options"])
    if ans["choice"] == "localhost":
        minikube_ip = "localhost"
        intersection_port = [8081, 8082, 8083]
        meta_service_port = 8084
    else:
        minikube_ip = os.popen('minikube ip').read().rstrip('\r\n')
        intersection_port = [30001, 30002, 30003]
        meta_service_port = 30004


def main():
    car_animation()
    print(title_art)
    local_or_minikube()
    global user_role

    while True:
        load_ids()
        print("\n\n")
        print(f"Current role: {user_role}")
        print("\n")
        if user_role is None:
            answer = prompt(interactive_menus["login_menu"])
            user_role = answer["role"]

            if answer["role"] == "Exit":
                exit()
            elif answer["role"] == "Emergency vehicle":
                access_token = login_emergency_vehicle(local = (minikube_ip == "localhost"))
            elif answer["role"] == "Mayor":
                access_token = login_mayor(local = (minikube_ip == "localhost"))
            elif answer["role"] == "TMC":
                access_token = login_tmc(local = (minikube_ip == "localhost"))
            elif answer["role"] == "Autonomous vehicle":
                access_token = login_autonomous_vehicle(local = (minikube_ip == "localhost"))
            else:
                access_token = login_autonomous_vehicle(local = (minikube_ip == "localhost"))

        # This part every time
        answer = prompt(interactive_menus["action_menu"])
        if answer["action"] == "Exit":
            exit()
        elif answer["action"] == "Logout":
            user_role = None
        elif answer["action"] == "Show lights status":
            traffic_light_data()
        elif answer["action"] == "Request green light":
            request_green_light_option(access_token)
        elif answer["action"] == "Change traffic lights state":
            set_state_at_intersection_option(access_token)
        elif answer["action"] == "List all intersections":
            list_all_intersections_option()
        elif answer["action"] == "Trigger Green Wave":
            set_green_wave_option(access_token)
        input("Press enter to continue")
        clear_console()


if __name__ == "__main__":
    main()
    # print(login_emergency_vehicle(local = True))
