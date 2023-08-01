FROM jboss/keycloak:16.1.0
MAINTAINER group8.ssas.tu.berlin
COPY keycloak.pem /etc/x509/https/tls.crt
COPY keycloak-key.pem /etc/x509/https/tls.key
COPY Duckburg-Traffic-Control-realm.json /tmp/Duckburg-Traffic-Control-realm.json
ENV KEYCLOAK_USER=admin
ENV KEYCLOAK_PASSWORD=admin
ENV PROXY_ADDRESS_FORWARDING=true
ENV KEYCLOAK_IMPORT=/tmp/Duckburg-Traffic-Control-realm.json