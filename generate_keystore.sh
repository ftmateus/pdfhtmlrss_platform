#!/bin/bash

PDFHTMLRSS_USER_DIRECTORY="$HOME/.pdfhtmlrss"

KEYSTORE_FILENAME="keystore.jks"

KEYSTORE_PATH="$PDFHTMLRSS_USER_DIRECTORY/$KEYSTORE_FILENAME"

#4 years
CA_VALIDITY=$((4*365))

if [ -f "$KEYSTORE_PATH" ]; then
  echo "Keystore already exists at $KEYSTORE_PATH"
  exit 1
fi

if [ ! -d "$PDFHTMLRSS_USER_DIRECTORY" ]; then
  mkdir "$PDFHTMLRSS_USER_DIRECTORY"
fi

echo "Generating CA RSA key pair..."

keytool -genkey -alias pdfhtmlrss_ca -keyalg RSA -keysize 4096 -validity $CA_VALIDITY -keystore "$KEYSTORE_PATH"

echo "Generating AES secret key (used to encrypt users private keys)..."

keytool -genseckey -alias pdfhtmlrss_encryption_key -keyalg AES -keysize 256 -keystore "$KEYSTORE_PATH"