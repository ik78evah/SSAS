# Test-Client
Makes the testing of our system a lot easier.

## Setup
1. Create new virtual environment
```bash
python -m venv venv
```
2. activate
```bash
source venv/bin/activate
```
3. install dependencies
```bash
pip install -r requirements.txt
```
4. Run the client
```bash
python tmc-fancy-cli.py
```

## Usage
The usage of our client is mostly self-explanatory. 
It provides the tester with default login credentials for all available roles and allows easy access to the systems functionality.

## Security
This client is not to be confused with a production-ready tmc-client implementation. 
In our final system, there will not exist any default accounts, especially not with default passwords. This version of the client
and the keycloak-database is solely meant to be used for the testing of the system's endpoints.
