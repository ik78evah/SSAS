import unittest
import client
import requests
import time


class TestAccessControlledEndpoint(unittest.TestCase):
    intersections = [1,2,3]
    def test_intersections_online(self):
        for i in self.intersections:
            request_url = f"http://{client.minikube_ip}:{client.intersection_port[i-1]}/intersection/status"
            resp = requests.get(request_url)
            self.assertTrue(resp.status_code==200)
            print(f"Intersection {i} is online")
        

    def test_requestGreen_as_Mayor(self):
        access_token = client.login_mayor()
        self.assertTrue(access_token is not None)
        for i in self.intersections:
            red_lights = client.find_red_traffic_lights(i)
            if len(red_lights) == 0:
                print(f"There are no red lights at intersection {i}")
                return
            request_green_light_url = f"http://{client.minikube_ip}:{client.meta_service_port}/meta/intersection/{i}/requestGreen/{red_lights[0]['id']['uuid']}"
            print(f"Requesting green light at {request_green_light_url}")
            resp = client.authorized_get_request(request_green_light_url, access_token)
            self.assertTrue(resp.status_code == 200)
            print("Success")


    def test_requestGreen_as_EmergencyVehicle(self):
        time.sleep(1)
        access_token = client.login_emergency_vehicle()
        self.assertTrue(access_token is not None)
        for i in self.intersections:
            red_lights = client.find_red_traffic_lights(i)
            if len(red_lights) == 0:
                print(f"There are no red lights at intersection {i}")
                return
            request_green_light_url = f"http://{client.minikube_ip}:{client.meta_service_port}/meta/intersection/{i}/requestGreen/{red_lights[0]['id']['uuid']}"
            print(f"Requesting green light at {request_green_light_url}")
            resp = client.authorized_get_request(request_green_light_url, access_token)
            self.assertTrue(resp.status_code == 200)
            print("Success")

        
        

if __name__ == "__main__":
    unittest.main()