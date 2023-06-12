@REM
@REM ************************************************************************************
@REM Description: run previously all batch files
@REM Author: Rui S. Moreira
@REM Date: 10/04/2018
@REM pwd: /Users/rui/Documents/NetBeansProjects/SD/src/edu/ufp/sd/
@REM ************************************************************************************

@REM ======================== Use Shell Parameters ========================
@REM Script usage: setenv <role> (where role should be: producer / consumer)
@Set SCRIPT_ROLE=%1

@REM ======================== CHANGE BELOW ACCORDING YOUR PROJECT and PC SETTINGS ========================
@REM ==== PC STUFF ====
@Set USERNAME=olaso

@Set WORKDRIVE=C
@Set JDK=C:\\Users\\olaso\\.jdks\\corretto-1.8.0_282
@Set NETBEANS=NetBeans
@Set INTELLIJ=IntelliJ
@Set CURRENT_IDE=%INTELLIJ%
@REM Set CURRENT_IDE=%NETBEANS%


@REM ==== JAVA NAMING STUFF ====
@Set JAVAPROJ_NAME=SD
@set JAVAPROJ=C:\\Users\\olaso\\IdeaProjects\\Projeto_SD_RabbitMQ
@Set RABBITMQ_SERVICES_FOLDER=edu/ufp/inf/sd/rmi
@Set RABBITMQ_SERVICES_PACKAGE=edu.ufp.inf.sd.rmi
@Set PACKAGE=Project
@Set QUEUE_NAME_PREFIX=Project
@Set EXCHANGE_NAME_PREFIX=exchange
@Set PRODUCER_CLASS_PREFIX=Send
@Set CONSUMER_CLASS_PREFIX=Recv
@Set OBSERVER_CLASS_PREFIX=ObserverGuiClient


@REM ==== NETWORK STUFF ====
@Set BROKER_HOST=localhost
@REM Set BROKER_PORT=15672
@Set BROKER_PORT=5672

@REM ======================== DO NOT CHANGE AFTER THIS POINT ========================
@Set JAVAPACKAGE=%RABBITMQ_SERVICES_PACKAGE%.%PACKAGE%
@Set JAVAPACKAGEROLE=%RABBITMQ_SERVICES_PACKAGE%.%PACKAGE%.%SCRIPT_ROLE%
@Set JAVAPACKAGEPATH=%RABBITMQ_SERVICES_FOLDER%/%PACKAGE%/%SCRIPT_ROLE%
@Set JAVASCRIPTSPATH=%RABBITMQ_SERVICES_FOLDER%/%PACKAGE%/runscripts
@Set BROKER_QUEUE=%QUEUE_NAME_PREFIX%_queue
@Set BROKER_EXCHANGE=%EXCHANGE_NAME_PREFIX%_exchange
@Set SERVICE_URL=http://%BROKER_HOST%:%BROKER_PORT%

@Set PATH=%PATH%;%JDK%/bin

IF "%CURRENT_IDE%"=="%NETBEANS%" (
    @Set JAVAPROJ_SRC=src
    @Set JAVAPROJ_CLASSES=build\\classes\\
    @Set JAVAPROJ_DIST=dist
    @Set JAVAPROJ_DIST_LIB=lib
)
IF "%CURRENT_IDE%"=="%INTELLIJ%" (
    @Set JAVAPROJ_SRC=src
    @Set JAVAPROJ_CLASSES=out\\production\\%JAVAPROJ_NAME%\\
    @Set JAVAPROJ_DIST=out\\artifacts\\%JAVAPROJ_NAME%
    @Set JAVAPROJ_DIST_LIB=lib
)

@Set JAVAPROJ_CLASSES_FOLDER=%JAVAPROJ%\\%JAVAPROJ_CLASSES%
@Set JAVAPROJ_JAR_FILE=%JAVAPROJ%\\%JAVAPROJ_DIST%\\%JAVAPROJ_NAME%.jar
@Set JAVA_LIB_FOLDER=%JAVAPROJ%\\%JAVAPROJ_DIST_LIB%
@Set JAVA_RABBITMQ_TOOLS=%JAVA_LIB_FOLDER%\\amqp-client-5.11.0.jar;%JAVA_LIB_FOLDER%\\slf4j-api-1.7.30.jar;%JAVA_LIB_FOLDER%\\slf4j-simple-1.7.30.jar

@REM Set CLASSPATH=%JAVAPROJ_CLASSES_FOLDER%;JAVA_RABBITMQ_TOOLS%
@REM Set CLASSPATH=%JAVAPROJ_JAR_FILE%
@Set CLASSPATH=.;%JAVAPROJ_JAR_FILE%;%JAVA_RABBITMQ_TOOLS%

@Set ABSPATH2CLASSES=%JAVAPROJ_CLASSES_FOLDER%
@Set ABSPATH2SRC=%JAVAPROJ%/%JAVAPROJ_SRC%









