import os
import json
import time
from typing import Any

import requests
import configparser

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

minikube_ip = os.popen('minikube ip').read().rstrip('\r\n')
print(f"The minikube ip is: {minikube_ip}")

username_emergency_vehicle = config['DefaultUsers']['EmergencyVehicleDefaultName']
pw_emergency_vehicle = config['DefaultUsers']['EmergencyVehicleDefaultPW']

intersection_port = [30001, 30002, 30003]
intersections = [1, 2, 3]
meta_service_port = 30004


def fetch_access_token_with_password(username: str, password: str, client_id: str = "green-light-request-client"):
    url = f"http://{minikube_ip}:31142/auth/realms/Duckburg-Traffic-Control/protocol/openid-connect/token"
    payload = {
        "client_id": client_id,
        "grant_type": "password",
        "username": username,
        "password": password
    }
    r = requests.post(url, data=payload)
    if r.status_code != 200:
        print(f"Something went wrong (status code: {r.status_code}")
        return ""
    return r.json()["access_token"]


def query_traffic_lights(intersection_nr: int):
    if intersection_nr <= 0 or intersection_nr > 3:
        print(f"There is no intersection with this number yet")
        return {}
    traffic_lights_query_request_url = f"http://{minikube_ip}:{intersection_port[intersection_nr - 1]}/intersection/trafficLights"
    r = requests.get(traffic_lights_query_request_url)
    if r.status_code != 200:
        print(f"Something went wrong (status code: {r.status_code} - url: {r.url}")
        return {}
    return r.json()


def find_red_traffic_lights(intersection_nr: int):
    traffic_lights_intersection1 = query_traffic_lights(intersection_nr)
    red_vehicle_traffic_lights = []
    for traffic_light in traffic_lights_intersection1:
        if traffic_light["type"] == "VEHICLE" and traffic_light["state"] == "RED":
            red_vehicle_traffic_light = traffic_light
            red_vehicle_traffic_lights.append(red_vehicle_traffic_light)
    return red_vehicle_traffic_lights


def print_login_options():
    print(">>> Login Options")
    print("1) log in as emergency-vehicle")
    print("2) log in as mayor")
    print("3) log in as autonomous-vehicle")
    print("4) log in as tmc")


def print_options():
    print("1) Show the traffic Lights at a specific intersection")
    print("2) Request Green Light")
    print("3) Set state of a specific intersection")
    print("4) Query the state of a specific traffic light")
    print("5) List all intersections")
    print("6) Logout")
    print("cancel - Cancels the program")


def get_user_choice(max_choice: int):
    while (True):
        user_input = input("\n What do you want to do? : ")
        print("")
        if 0 < int(user_input) <= max_choice:
            print("Good Choice!")
            return int(user_input)
        if user_input == "cancel":
            print("As you wish.")
            exit()
        print("This was sadly not an option.")
        print_options()


def log_in(username: str, password: str):
    access_token = fetch_access_token_with_password(username, password)
    if access_token == "":
        print("Something went wrong. Please try again.")
        exit()
    print(" ")
    print(f"-----  Access-Token for {username} ------")
    print(access_token)
    print(f"------------------------------------------")
    return access_token


def login_emergency_vehicle():
    username = username_emergency_vehicle
    password = pw_emergency_vehicle
    return log_in(username, password)


def login_mayor():
    username = config['DefaultUsers']['MayorDefaultName']
    password = config['DefaultUsers']['MayorDefaultPW']
    return log_in(username, password)


def login_autonomous_vehicle():
    username = config['DefaultUsers']['RandomRudolf']
    password = config['DefaultUsers']['RandomRudolf']
    return log_in(username, password)


def login_tmc():
    username = config['DefaultUsers']['TMC']
    password = config['DefaultUsers']['TMC_pw']
    access_token = fetch_access_token_with_password(username, password)
    if access_token == "":
        print("Something went wrong. Please try again.")
        exit()
    print(" ")
    print(f"-----  Access-Token for {username} ------")
    print(access_token)
    print(f"------------------------------------------")
    return access_token


def query_traffic_lights_option():
    user_input = get_user_choice_intersection()
    url = f"http://{minikube_ip}:{meta_service_port}/meta/intersection{user_input}/trafficLights"
    r = requests.get(url)
    if r.status_code != 200:
        print(f"Something went wrong (status code: {r.status_code} - url: {r.url}")
        return {}
    print("------------------------------------------")
    print(f"Traffic Lights at Intersection {user_input}: ", json.dumps(r.json(), indent=5, sort_keys=True))
    print("------------------------------------------\n")


def get_user_choice_intersection():
    while True:
        user_input = input(f"Which intersection? {intersections}: ")
        intersection_choice = int(user_input)
        if intersection_choice not in intersections:
            print("Not a valid option, sorry.")
            continue
        break
    return intersection_choice


def get_user_choice_red_lights(red_lights):
    if len(red_lights) == 0:
        print("There are not red lights at this intersection.")
        exit()
    print("red traffic lights at this intersection:")
    i = 1
    for light in red_lights:
        print("---")
        print(f"Option {i}")
        print(f">> uuid: {light['id']['uuid']}")
        print(f">> location: {light['id']['location']}")
        print(f">> state: {light['state']}")
        i += 1
    options = [x for x in range(1, i)]
    while True:
        traffic_light_choice = input(f"At which traffic light do you want to request a green light? {options}: ")
        traffic_light_choice = int(traffic_light_choice)
        if traffic_light_choice not in options:
            print("Not a valid option, sorry")
            continue
        break
    return traffic_light_choice


def authorized_get_request(url: str, access_token: str):
    headers = {
        "Authorization": f"Bearer {access_token}"
    }
    resp = requests.get(url, headers=headers)
    if resp.status_code != 200:
        print(f"Something went wrong I am afraid. Debug info:")
        print(f"status code: {resp.status_code}")
        print(f"body: {resp.text}")
        print(f"request: {resp.request.headers}")
        exit()
    return resp


def authorized_post_request(url: str, payload: Any, access_token: str):
    headers = {
        "Authorization": f"Bearer {access_token}"
    }
    resp = requests.post(url, headers=headers, json=payload)
    if resp.status_code != 200:
        print(f"Something went wrong I am afraid. Debug info:")
        print(f"status code: {resp.status_code}")
        print(f"body: {resp.text}")
        print(f"request: {resp.request.headers}")
        exit()
    return resp


def request_green_light_option(access_token: str):
    intersection_choice = get_user_choice_intersection()
    red_lights = find_red_traffic_lights(intersection_choice)
    traffic_light_choice = get_user_choice_red_lights(red_lights)
    traffic_light_id = red_lights[traffic_light_choice - 1]['id']['uuid']

    request_green_light_url = f"http://{minikube_ip}:{meta_service_port}/meta/intersection/{intersection_choice}/requestGreen/{traffic_light_id}"
    resp = authorized_get_request(request_green_light_url, access_token)
    print("Request granted :)")
    print("The traffic light will now transition to green and is blocked until:")
    print(f">> {resp.text}")


def request_status_as_request(intersection_nr : int):
    url_mock = f"http://{minikube_ip}:{meta_service_port}/meta/intersection{intersection_nr}/statusAsRequest"
    payload = requests.get(url_mock)
    if payload.status_code != 200:
        print(f"Something went wrong (status code: {payload.status_code} - url: {payload.url}")
        exit()
    return payload


def set_state_at_intersection_option(access_token: str):
    user_input = get_user_choice_intersection()
    url_post = f"http://{minikube_ip}:{intersection_port[user_input - 1]}/intersection/requestState"
    payload = request_status_as_request(user_input)
    print("------------------------------------------")
    print(f"Template state change: ", json.dumps(payload.json()))  # , indent=5, sort_keys=True))
    print("------------------------------------------\n")
    payload2 = input("Change states in template and insert: ")
    print("------------------------------------------\n")
    print("")
    requested_state = json.loads(payload2)
    authorized_post_request(url_post, payload=requested_state, access_token=access_token)
    print("Success :)")
    time.sleep(1)
    new_state = request_status_as_request(user_input)
    print("-----------")
    print(">> New State:")
    print(json.dumps(new_state.json()))
    print("-------------")
    return int(user_input)


def query_specific_traffic_light_state_option():
    intersection_nr = get_user_choice_intersection()
    traffic_lights = query_traffic_lights(intersection_nr)

    options = [x for x in range(1,len(traffic_lights)+1)]
    while True:
        traffic_light = input(f"Which traffic light? {options}:")
        traffic_light = int(traffic_light)
        if traffic_light not in options:
            print("Not a valid option!")
            continue
        break

    traffic_light_uuid = traffic_lights[traffic_light - 1]['id']['uuid']

    query_traffic_light_state_url = f"http://{minikube_ip}:{meta_service_port}/meta/intersection{intersection_nr}/trafficLights/{traffic_light_uuid}"
    resp = requests.get(query_traffic_light_state_url)
    if resp.status_code != 200:
        print(f"Something went wrong I am afraid. Debug info:")
        print(f"status code: {resp.status_code}")
        print(f"body: {resp.text}")
        print(f"request: {resp.request.headers}")
        exit()
    traffic_light_json = resp.json()
    print(json.dumps(traffic_light_json))


def list_all_intersections_option():
    print("")
    url_meta = f"http://{minikube_ip}:{meta_service_port}/meta"
    r_meta = requests.get(url_meta)
    if r_meta.status_code != 200:
        print(f"Something went wrong (status code: {r_meta.status_code} - url: {r_meta.url}")
        return {}
    json_data_meta = r_meta.json()

    for x in range(1, 4):
        intersection_query_request_url = f"http://{minikube_ip}:{meta_service_port}/meta/intersection{x}"
        r = requests.get(intersection_query_request_url)
        if r.status_code != 200:
            print(f"Something went wrong (status code: {r.status_code} - url: {r.url}")
            return {}
        json_data = r.json()
        print(f"Info of intersection {x}:")
        status_row = json_data_meta[f"Status Intersection{x}"]
        print(f"Status: {status_row}")
        info_row = json_data["Info"]
        print(f"This is the {info_row}")
        lat_row = json_data["Latitude"]
        long_row = json_data["Longitude"]
        print(f"It is located at a Latitude of {lat_row} and a Longitude of {long_row}")
        connected = json_data["trafficLightsConnected"]
        print(f"The number of connected traffic lights is: {connected}")
        blocked_until = json_data["blockedUntil"]
        if (blocked_until == None):
            print("The intersection is open for requests")
        else:
            print(f"The intersection can not take any more requests until: {blocked_until}")
        print("")


def main():
    print(title_art)
    # Login
    print_login_options()
    uc = get_user_choice(4)
    access_token = ""
    if uc == 1:
        access_token = login_emergency_vehicle()
    elif uc == 2:
        access_token = login_mayor()
    elif uc == 3:
        access_token = login_autonomous_vehicle()
    elif uc == 4:
        access_token = login_tmc()

    # Action
    while (True):
        print("\n\n\n")
        print("----------------------------------------------------")
        print_options()
        uc = get_user_choice(6)
        if uc == 1:
            query_traffic_lights_option()
        elif uc == 2:
            request_green_light_option(access_token)
        elif uc == 3:
            set_state_at_intersection_option(access_token)
        elif uc == 4:
            query_specific_traffic_light_state_option()
        elif uc == 5:
            list_all_intersections_option()
        elif uc == 6:
            main()
        input("Press enter to continue")


if __name__ == "__main__":
    main()
