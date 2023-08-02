#!/bin/bash

# Create a server certificate for the intersection 1
cfssl gencert -ca target/intermediate_ca.pem -ca-key target/intermediate_ca-key.pem -config cfssl.json -profile=server intersection1.json | cfssljson -bare target/intersection-1-server

# Create a PKCS12 file, can also be read/used by keytool (keystore type is pkcs12)
openssl pkcs12 -export -inkey target/intersection-1-server-key.pem -in <(cat target/intersection-1-server.pem target/intermediate_ca.pem) -name intersection1 -out target/intersection1.p12

# Create a server certificate for the intersection 2
cfssl gencert -ca target/intermediate_ca.pem -ca-key target/intermediate_ca-key.pem -config cfssl.json -profile=server intersection2.json | cfssljson -bare target/intersection-2-server

# Create a PKCS12 file, can also be read/used by keytool (keystore type is pkcs12)
openssl pkcs12 -export -inkey target/intersection-2-server-key.pem -in <(cat target/intersection-2-server.pem target/intermediate_ca.pem) -name intersection2 -out target/intersection2.p12

# Create a server certificate for the intersection 3
cfssl gencert -ca target/intermediate_ca.pem -ca-key target/intermediate_ca-key.pem -config cfssl.json -profile=server intersection3.json | cfssljson -bare target/intersection-3-server

# Create a PKCS12 file, can also be read/used by keytool (keystore type is pkcs12)
openssl pkcs12 -export -inkey target/intersection-3-server-key.pem -in <(cat target/intersection-3-server.pem target/intermediate_ca.pem) -name intersection3 -out target/intersection3.p12
