#!/bin/bash

mvn package

neo4j stop
cp target/*.jar /usr/local/Cellar/neo4j/2.2.2/libexec/plugins
neo4j start

curl http://localhost:7474/db/data/ext/ExpandConcepts
