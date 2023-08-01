#!/bin/bash

# Create a peer certificate for the meta intersection service
cfssl gencert -ca target/intermediate_ca.pem -ca-key target/intermediate_ca-key.pem -config cfssl.json -profile=peer meta.json | cfssljson -bare target/meta


# Create a PKCS12 file, can also be read/used by keytool (keystore type is pkcs12)
openssl pkcs12 -export -inkey target/meta-key.pem -in <(cat target/meta.pem target/intermediate_ca.pem) -name meta -out target/meta.p12
  