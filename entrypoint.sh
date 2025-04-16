#!/bin/bash

if [ "$ENV_BUILD" = "LOCAL"]; then
	AGENT_LIB="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
else
	AGENT_LIB=""

exec "java $AGENT_LIB -jar /home/app/StorageInterface/target/StorageInterface-0.0.1-SNAPSHOT.jar"