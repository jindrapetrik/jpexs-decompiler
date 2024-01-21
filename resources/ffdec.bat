@echo off
rem This is a comment, it starts with "rem".

rem Uncomment following and set it to higher value if you want more memory:
rem You need 64 bit OS and 64 bit java to set it to higher values
rem set FFDEC_MEMORY=1024m

rem Uncomment following when you encounter StackOverFlowErrors. 
rem If the app then terminates with OutOfMemory you can experiment with lower value.
rem set FFDEC_STACK_SIZE=32m

rem Hide VLC error output
set VLC_VERBOSE=-1

if "%FFDEC_MEMORY%"=="" set FFDEC_MEMORY=1024m

set STACK_SIZE_PARAM=
set MEMORY_PARAM=
if not "%FFDEC_STACK_SiZE%"=="" set STACK_SIZE_PARAM= -Xss%FFDEC_STACK_SiZE% 
if not "%FFDEC_MEMORY%"=="" set MEMORY_PARAM=-Xmx%FFDEC_MEMORY% 

java %MEMORY_PARAM%%STACK_SIZE_PARAM%-Djna.nosys=true -jar "%~dp0\ffdec.jar" %*
