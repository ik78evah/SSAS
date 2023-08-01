#!/bin/bash

# Create trust an dimport Root CA
keytool -importcert -noprompt -file target/rootCA.pem -alias root -trustcacerts -keystore target/truststore.jks
keytool -importcert -noprompt -file target/intermediate_ca.pem -alias intermediate_ca -trustcacerts -keystore target/truststore.jks

keytool -importkeystore -srckeystore target/truststore.jks -destkeystore truststore.p12 -srcstoretype JKS -deststoretype PKCS12

openssl pkcs12 -in truststore.p12 -out truststore.pem