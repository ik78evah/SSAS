#!/bin/bash

# Create a server certificate for the keycloak server
cfssl gencert -ca target/intermediate_ca.pem -ca-key target/intermediate_ca-key.pem -config cfssl.json -profile=server keycloak.json | cfssljson -bare target/keycloak

# Create a PKCS12 file, can also be read/used by keytool (keystore type is pkcs12)
openssl pkcs12 -export -inkey target/keycloak-key.pem -in <(cat target/keycloak.pem target/intermediate_ca.pem) -name keycloak -out target/keycloak.p12
