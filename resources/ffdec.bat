@echo off
rem Set following to higher value if you want more memory:
rem You need 64 bit OS and 64 bit java to set it to higher values
set MEMORY=1024m

rem Set following to higher value when you encounter StackOverFlowErrors
set STACK_SIZE=32m

rem Hide VLC error output
set VLC_VERBOSE=-1

java -Xmx%MEMORY% -Xss%STACK_SIZE% -Djna.nosys=true -jar "%~dp0\ffdec.jar" %*