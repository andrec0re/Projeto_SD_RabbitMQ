#!/usr/bin/env bash
#REM ************************************************************************************
#REM Description: start naming service (rmi registry)
#REM Author: Rui S. Moreira
#REM Date: 20/02/2014
#REM ************************************************************************************
source ./setenv.sh server
cd ${ABSPATH2CLASSES}
echo "Run RMI registry on directory:"
echo ${ABSPATH2CLASSES}
#clear
#pwd
#rmiregistry &
rmiregistry