#!/bin/bash

export TARGET=`brew ls --verbose neo4j | grep plugins/README.txt | sed s/README\.txt//`

mvn package

neo4j stop
cp target/*.jar $TARGET
neo4j start

echo Installed plugin into $TARGET
