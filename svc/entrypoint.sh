#!/bin/bash

version=$(grep "artifactId" pom.xml -A 1 | grep version | head -n 1 | sed "s|[^>]*>\([^<]*\).*|\1|")
echo "App Version: $version"

java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar app.jar
