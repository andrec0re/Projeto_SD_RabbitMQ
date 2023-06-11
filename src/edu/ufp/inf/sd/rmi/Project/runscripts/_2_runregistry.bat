@REM *************************************************
@REM Description: start rmi registry
@REM Author: Rui Moreira
@REM Date: 20/02/2005
@REM *************************************************
call setenv

echo Run RMI registry on fiolder:
@cd %ABSPATH2CLASSES%

@REM cls
@REM start rmiregistry
rmiregistry