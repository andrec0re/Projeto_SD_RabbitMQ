#!/usr/bin/env bash
#REM ************************************************************************************
#REM Description: run HelloWorldClient
#REM Author: Rui S. Moreira
#REM Date: 20/02/2014
#REM ************************************************************************************
#REM Script usage: runclient <role> (where role should be: server / client)
#source ./setclientenv.sh
source ./setenv.sh server

#Run python on *classes* or *dist* directory
cd ${ABSPATH2CLASSES}
#cd ${ABSPATH2DIST}
#clear
echo ${ABSPATH2CLASSES}
#Run python 3:
#python3 -m http.server 8000
#Run python 2.7:
python -m SimpleHTTPServer 8000

cd ${ABSPATH2SRC}/${JAVASCRIPTSPATH}