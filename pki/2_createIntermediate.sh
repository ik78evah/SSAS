#!/bin/bash

# Generates a intermediate CA certificate
cfssl gencert -initca intermediate-ca.json | cfssljson -bare target/intermediate_ca

# Use Root CA to sign the intermediate CA certificate
cfssl sign -ca target/rootCA.pem -ca-key target/rootCA-key.pem -config cfssl.json -profile intermediate_ca target/intermediate_ca.csr | cfssljson -bare target/intermediate_ca
