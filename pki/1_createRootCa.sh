#!/bin/bash

# Generates a self-signed root CA certificate
cfssl gencert -initca csr_ca.json | cfssljson -bare target/rootCA
