@REM ************************************************************************************
@REM Description: run
@REM Author: Rui S. Moreira
@REM Date: 10/04/2018
@REM ************************************************************************************
@REM Script usage: runclient <role> (where role should be: producer / consumer)
call setenv project_rabbit

cd %ABSPATH2CLASSES%
java -cp %CLASSPATH% %JAVAPACKAGEROLE%.%OBSERVER_CLASS_PREFIX% %BROKER_HOST% %BROKER_PORT% %BROKER_EXCHANGE% %BROKER_QUEUE% %BROKER_QUEUE_FRONTSERVER% %1

cd %ABSPATH2SRC%/%JAVASCRIPTSPATH%