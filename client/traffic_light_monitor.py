from __future__ import print_function, unicode_literals
import os
import json
import time

from tabulate import tabulate
import requests
from PyInquirer import prompt, Separator

minikube_ip = 0
intersection_port = 0
meta_service_port = 0


with open('menus.json') as menus:
    interactive_menus = json.load(menus)


def state_color(state) -> str:
    if state == "GREEN":
        return "\033[92m GREEN \033[0m"
    elif state == "RED":
        return "\033[91m RED   \033[0m"
    elif state == "YELLOW":
        return "\033[93m YELLOW \033[0m"
    elif state == "RED_YELLOW":
        return "\033[91m RED_\033[93m YELLOW \033[0m"
    elif state == "EMERGENCY":
        return "\033[43m EMERGENCY \033[0m"


def traffic_light_to_string(light, type, location) -> str:
    res = [light["id"]["uuid"], state_color(light["state"])]
    if type:
        res.append(" \033[1m" + light["type"] + "\033[0m")
        res.append(" \033[1m" + light["trafficLightPositionTag"] + "\033[0m")
    if location:
        res.append(" \033[2m" + str(light["id"]["location"]["latitude"]) + "\033[0m")
        res.append(" \033[2m" + str(light["id"]["location"]["longitude"]) + "\033[0m")
    return res


def query_traffic_lights(inter, loc):
    url = f"https://{minikube_ip}:{meta_service_port}/meta/intersection/{inter}/trafficLights"
    r = requests.get(url, verify="truststore.pem")
    if r.status_code != 200:
        print(f"\033[91mIntersection {inter} unavailable (status code: {r.status_code} - url: {r.url}\033[0m")
        return
    data = r.json()
    print(data)
    print("\033[92m Intersection" + str(inter) + "\033[0m")
    tl_table = []
    for item in data:
        tl_table.append(traffic_light_to_string(item, True, loc))
    if loc:
        return tabulate(tl_table, ["ID", "State", "Type", "Position-Tag", "Latitude", "Longitude"], tablefmt="grid")
    else:
        return tabulate(tl_table, ["ID", "State", "Type", "Position-Tag"], tablefmt="grid")


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


def clear_console():
    os.system('cls' if os.name == 'nt' else 'clear')


def intersection_picker():
    question = interactive_menus["intersections_menu"]
    question[0]["choices"] = []
    for k in range(1, 4):
        url = f"https://{minikube_ip}:{meta_service_port}/meta/intersection/{k}/trafficLights"
        r = requests.get(url, verify="truststore.pem")
        if r.status_code == 200:
            question[0]["choices"].append(str(k))
    intersection = prompt(question)
    return int(intersection["intersectionNr"])


def main():
    local_or_minikube()
    intersection = intersection_picker()
    answer = prompt(interactive_menus["refresh_time"])
    delay = int(answer["time"])
    location = False

    while True:
        table = query_traffic_lights(intersection, location)
        clear_console()
        print("\033[92m Intersection" + str(intersection) + "\033[0m")
        print(table)
        time.sleep(delay)


if __name__ == "__main__":
    main()
