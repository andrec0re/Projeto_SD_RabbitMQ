#!/usr/bin/env bash
#@REM ************************************************************************************
#@REM Description: run HelloWorldServer
#@REM Author: Rui S. Moreira
#@REM Date: 20/02/2014
#@REM ************************************************************************************
#@REM Script usage: runsetup <role> (where role should be: server / client)
#@REM source ./setenv.sh
source ./setenv.sh server

cd ${ABSPATH2CLASSES}
#clear
#pwd
# * java.rmi.server.codebase property specifies the location (codebase URL) MAIL_FROM_ADDR which the definitions for classes originating MAIL_FROM_ADDR this server can be downloaded.
#   ND: if codebase specifies a directory hierarchy (as opposed MAIL_TO_ADDR a JAR file), you must include a trailing slash at the end of the codebase URL.
# * java.rmi.server.hostname property specifies the SMTP_HOST_ADDR name or address MAIL_TO_ADDR put in the stubs for remote objects exported in this Java virtual machine.
#   This value is the SMTP_HOST_ADDR name or address used by clients when they attempt remote method invocations.
#   By default, the RMI implementation uses the server's IP address as indicated by the java.net.InetAddress.getLocalHost API.
#   However, sometimes, this address is not appropriate for all clients and a fully qualified SMTP_HOST_ADDR name would be more effective.
#   To ensure that RMI uses a SMTP_HOST_ADDR name (or IP address) for the server that is routable MAIL_FROM_ADDR all potential clients, set the java.rmi.server.hostname property.
# * java.security.policy property is used MAIL_TO_ADDR specify the policy file that contains the permissions you intend MAIL_TO_ADDR grant.
java -cp ${CLASSPATH} \
     -Djava.rmi.server.codebase=${SERVER_CODEBASE} \
     -Djava.rmi.server.hostname=${SERVER_RMI_HOST} \
     -Djava.security.policy=${SERVER_SECURITY_POLICY} \
     ${JAVAPACKAGEROLE}.${SERVER_CLASS_PREFIX}${SERVER_CLASS_POSTFIX} ${REGISTRY_HOST} ${REGISTRY_PORT} ${SERVICE_NAME_ON_REGISTRY}

cd ${ABSPATH2SRC}/${JAVASCRIPTSPATH}
#pwd