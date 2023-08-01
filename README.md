# duckburg-traffic-light-control-gruppe-8 Project
In this project, the traffic lights at three intersections should be managed. 
Each intersection has four vehicle traffic lights and eight pedestrian traffic lights, as shown below.
The traffic lights should be controllable by the traffic light management center. A UI for operators should be provided.
Autonomous vehicles should be able to request the state a traffic light.
Emergency vehicles and the mayor's car should be able to request a green light with priority for the emergency vehicle.
All systems have to be safe for all users at all times and robust against cyberattacks.

![Intersection](https://github.com/ik78evah/SSAS/assets/73646817/2192415c-da65-4790-a680-bfe1dc0de0a8)

## Component diagram
This diagram illustrates the IT components existing in this solution. To view further illustrations (UML and sequence diagram) please view further diagrams

![SSAS_Komponentendiagramm_finale_Abgabe drawio](https://github.com/ik78evah/SSAS/assets/73646817/916a480b-f3aa-4d47-b74c-7e234f16e6bd)


This project uses Quarkus, the Supersonic Subatomic Java Framework.

## 1. Get Project
```shell
git clone https://git.tu-berlin.de/sssas-wise-21-22-gruppe-8/duckburg-traffic-light-control-gruppe-8.git
```
***

## 2. Switch to dev branch
```shell
git checkout dev_clean
```
***

## 3. Build & Test
```shell
mvn clean install
```
***

## 4. Running the application in dev mode

You can run this application in dev mode that enables live coding using:
### Run intersection1-service on localhost:8081
```shell script
cd intersection-service-group-8/ 
mvn compile quarkus:dev 
```

### Run intersection2-service on localhost:8082
```shell script
cd intersection-service-group-8/ 
mvn compile quarkus:dev -Dquarkus-profile=intersection2
```

### Run intersection3-service on localhost:8083
```shell script
cd intersection-service-group-8/ 
mvn compile quarkus:dev -Dquarkus-profile=intersection3
```

### Run meta-intersection-service on localhost:8084
```shell script
cd meta-intersection-service-group-8/ 
mvn compile quarkus:dev
```
***

### Setup Keycloak locally
```bash
cd keycloak
docker build -t ssas-group8/keycloak-ssl:latest . #this could take some minutes the first time, sorry
docker run -p 31141:8443 -p 31142:8080 ssas-group8/keycloak-ssl:latest
```
start should look like this: <br>
![img.png](images/img5.png)

end should look like this: <br>
![img.png](images/img6.png)


## 5. curl examples to manual test service in dev mode <br> (use client instead is recommended -> see 7.)
```shell 
cd client
curl --cacert truststore.pem https://localhost:8084/meta
curl --cacert truststore.pem https://localhost:8084/meta/intersection/2
curl --cacert truststore.pem https://localhost:8084/meta/intersection/2/trafficLights
curl --cacert truststore.pem https://localhost:8084/meta/intersection/2/trafficLights/<insert specific uuid>
curl --cacert truststore.pem https://localhost:8084/meta/intersection/2/trafficLights/<insert specific uuid>/getStatus
curl --cacert truststore.pem https://localhost:8084/meta/intersection/2/trafficLights/<insert specific uuid>/getState
```
***

## 6. Packaging and running the application in minikube
```shell
# minikube delete --all -> will delete everything so you can start from scratch
minikube start
eval $(minikube docker-env) #bash
minikube docker-env | Invoke-Expression #powershell

# build and deploy the keycloak server
cd keycloak
docker build -t ssas-group8/keycloak-ssl:latest . #this could take some minutes the first time, sorry
kubectl apply -f keycloak.yaml 

cd .. 

minikube ip
# please insert your minikube ip into quarkus.oidc.auth-server-url at line 18 
# of the intersection property file (application.properties) and line 19 in meta-intersection property file


mvn clean package -Dquarkus.kubernetes.deploy=true #bash
mvn clean package '-Dquarkus.kubernetes.deploy=true' #powershell
```

### Test deployment
```shell
minikube service list
```
output should look like this: <br>
![img.png](images/img1.png)

***

## 7. TMC-(Fancy-)Client 

### Setup
1. Create new virtual environment
```bash
cd client
python -m venv venv
```
2. activate
```bash
# unix
source venv/bin/activate 

# windows:
sourve venv/Scripts/activate
```
3. install dependencies
```bash
pip install -r requirements.txt
```

### Run Client
```shell
cd client
python tmc-fancy-cli.py
```
second window 
```shell
cd client
source venv/bin/activate 
python traffic_light_monitor.py
```

## manual curl commands (use client instead)
```shell
curl -X POST -H "Content-Type: application/json" \
    -d 'request_body' \
localhost:8079/client/meta/intersection1/requestState/
```

## Usage
The usage of our client is mostly self-explanatory.
It provides the tester with default login credentials for all available roles and allows easy access to the systems functionality.

## Security
This client is not to be confused with a production-ready tmc-client implementation.
In our final system, there will not exist any default accounts, especially not with default passwords. This version of the client
and the keycloak-database is solely meant to be used for the testing of the system's endpoints.


```
#### OAuth
- Request Token (with emergency-vehicle-credentials)
```shell
curl --request POST \
--url http://$(minikube ip):31142/auth/realms/Duckburg-Traffic-Control/protocol/openid-connect/token \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data client_id=green-light-request-client \
--data grant_type=password \
--data username=fire-brigade-duckburg-vehicle-1 \
--data password=pqhoxishw
```

- Use the access token from above to request a green light
```shell
curl --request GET \
  --url http://$(minikube ip)/intersection/requestGreen/0071bb9e-fd79-4975-a48f-c8bbb7f99200 \
  --header 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJkc0xuZGM5M2JkekJtNUxEZHRDQXE5aDM4RjFUTEZWTG4xTzZzUDRwelpJIn0.eyJleHAiOjE2NDIwMzU5OTIsImlhdCI6MTY0MjAzNTY5MiwianRpIjoiNzU1NDc1MGUtZDA4MS00NDc0LWJiYjctZGU4MzQ2YzJlMmM2IiwiaXNzIjoiaHR0cDovLzE5Mi4xNjguNDkuMjozMTE0Mi9hdXRoL3JlYWxtcy9EdWNrYnVyZy1UcmFmZmljLUNvbnRyb2wiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiODc1ZGY0ZTQtMGI0OS00N2U0LTk3ZTItYzlhNjI2MTkyMjc0IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiZ3JlZW4tbGlnaHQtcmVxdWVzdC1jbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiMTg2NWIwYjctMWVmMi00MjMwLWI3ZGYtZGYwNTNjZDJjZmVjIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLWR1Y2tidXJnLXRyYWZmaWMtY29udHJvbCIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJncmVlbi1saWdodC1yZXF1ZXN0LWNsaWVudCI6eyJyb2xlcyI6WyJhdXRvbm9tb3VzLXZlaGljbGUiLCJlbWVyZ2VuY3ktdmVoaWNsZSJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudC
```
- If the request was successfull, the response body should include a timestamp. 
- Until this timestamp the intersection will be blocked and the requested traffic light will be green.

### Lens
- download: https://k8slens.dev/desktop.html
- after deployment to minikube open Lens and connect to minikube cluster
- go to Workloads -> Pods
- should look like this:

![img.png](images/img2.png)
- open logs, e.g. for meta-intersection-service:

![img.png](images/img3.png)
- `curl 172.24.163.194:30004/meta`
- `curl 172.24.163.194:30004/meta/intersection/3`

![img.png](images/img4.png)
