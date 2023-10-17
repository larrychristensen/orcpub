#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
openssl req \
	-subj "/C=PL/ST=Warsaw/L=Warsaw/O=Orcpub Web/OU=Orcpub/CN=${PWD##*/}" \
	-x509 \
	-nodes \
	-days 365 \
	-newkey rsa:2048 \
	-keyout "${DIR}/snakeoil.key" \
	-out "${DIR}/snakeoil.crt"
