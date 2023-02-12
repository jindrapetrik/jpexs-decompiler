@echo off
rem This is a comment, it starts with "rem".

rem Set following to higher value if you want more memory:
rem You need 64 bit OS and 64 bit java to set it to higher values
set MEMORY=1024m

rem Uncomment following when you encounter StackOverFlowErrors. 
rem If the app then terminates with OutOfMemory you can experiment with lower value.
rem set STACK_SIZE=32m

rem Hide VLC error output
set VLC_VERBOSE=-1

if not "%STACK_SiZE%"=="" set STACK_SIZE_PARAM= -Xss%STACK_SiZE% 
if not "%MEMORY%"=="" set MEMORY_PARAM=-Xmx%MEMORY% 

java %MEMORY_PARAM%%STACK_SIZE_PARAM%-Djna.nosys=true -jar "%~dp0\ffdec.jar" %*