#!/usr/bin/env bash
source ./setenv.sh client

cd "${ABSPATH2CLASSES}"

java -cp "${CLASSPATH}" \
     -Djava.security.policy="${CLIENT_SECURITY_POLICY}" \
     -Djava.rmi.server.codebase="${SERVER_CODEBASE}" \
     -D${JAVAPACKAGEROLE}.codebase="${CLIENT_CODEBASE}" \
     -D${JAVAPACKAGE}.servicename="${SERVICE_NAME_ON_REGISTRY}" \
     "${JAVAPACKAGEROLE}"."${CLIENT_CLASS_PREFIX}""${CLIENT_CLASS_POSTFIX}" "${REGISTRY_HOST}" "${REGISTRY_PORT}" "${SERVICE_NAME_ON_REGISTRY}"

cd "${ABSPATH2SRC}"/"${JAVASCRIPTSPATH}"
