#!/usr/bin/env bash

if [ -z "$ADMIN_PASSWORD" ]; then
  echo "Environment variable ADMIN_PASSWORD not set. See https://docs.datomic.com/on-prem/configuring-embedded.html#sec-2-1"
  exit 1
fi

if [ -z "$DATOMIC_PASSWORD" ]; then
  echo "Environment variable DATOMIC_PASSWORD not set. See https://docs.datomic.com/on-prem/configuring-embedded.html#sec-2-1"
  exit 1
fi

sed "/host=datomic/a alt-host=${ALT_HOST:-127.0.0.1}" -i transactor.properties
sed "s/# storage-admin-password=/storage-admin-password=${ADMIN_PASSWORD}/" -i transactor.properties
sed "s/# storage-datomic-password=/storage-datomic-password=${DATOMIC_PASSWORD}/" -i transactor.properties

if [ -n "$ADMIN_PASSWORD_OLD" ]; then
  sed "s/# old-storage-admin-password=/old-storage-admin-password=$ADMIN_PASSWORD_OLD/" -i transactor.properties
fi

if [ -n "$DATOMIC_PASSWORD_OLD" ]; then
  sed "s/# old-storage-datomic-password=/old-storage-datomic-password=$DATOMIC_PASSWORD_OLD/" -i transactor.properties
fi

sed "s/# encrypt-channel=true/encrypt-channel=${ENCRYPT_CHANNEL:-true}/" -i transactor.properties

bin/transactor transactor.properties