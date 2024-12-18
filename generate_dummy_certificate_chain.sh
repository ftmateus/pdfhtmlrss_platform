#!/bin/bash

openssl genrsa -out rootCA.key 2048

openssl req -x509 -new -nodes -key rootCA.key -sha256 -days 1024 -out rootCA.pem \
       -subj "/C=US/ST=Test State/L=Test City/O=Test Org/OU=Test CA/CN=Test Root CA"

openssl genrsa -out intermediateCA.key 2048

openssl req -new -key intermediateCA.key -out intermediateCA.csr \
    -subj "/C=US/ST=Test State/L=Test City/O=Test Org/OU=Test Intermediate CA/CN=Test Intermediate CA"

openssl x509 -req -in intermediateCA.csr -CA rootCA.pem -CAkey rootCA.key -CAcreateserial \
    -out intermediateCA.pem -days 500 -sha256

openssl genrsa -out leaf.key 2048

openssl req -new -key leaf.key -out leaf.csr \
    -subj "/C=US/ST=Test State/L=Test City/O=Test Org/OU=Test Leaf/CN=localhost"

openssl x509 -req -in leaf.csr -CA intermediateCA.pem -CAkey intermediateCA.key -CAcreateserial \
    -out leaf.pem -days 365 -sha256





