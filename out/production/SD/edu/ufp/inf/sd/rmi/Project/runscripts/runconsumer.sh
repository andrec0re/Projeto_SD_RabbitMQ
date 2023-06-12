#!/usr/bin/env bash
#REM ************************************************************************************
#REM Description: run 
#REM Author: Rui S. Moreira
#REM Date: 10/04/2018
#REM ************************************************************************************
#REM Script usage: runclient <role> (where role should be: producer / consumer)
source ./setenv.sh consumer

echo ${ABSPATH2CLASSES}
cd ${ABSPATH2CLASSES}
#clear
#pwd
java -cp ${CLASSPATH} \
     ${JAVAPACKAGEROLE}.${CONSUMER_CLASS_PREFIX} ${BROKER_HOST} ${BROKER_PORT} ${BROKER_QUEUE}

echo ${ABSPATH2SRC}/${JAVASCRIPTSPATH}
cd ${ABSPATH2SRC}/${JAVASCRIPTSPATH}
#pwd