#!/usr/bin/dumb-init /bin/sh

# Start datomic DB
/datomic/bin/transactor /datomic/transactor.properties &

# Start orcpub app
java -jar /orcpub.jar
