@echo off
rem This is a comment, it starts with "rem".

rem Uncomment following and set it to higher value if you want more memory:
rem You need 64 bit OS and 64 bit java to set it to higher values
rem set FFDEC_MEMORY=1024m

rem Uncomment following when you encounter StackOverFlowErrors. 
rem If the app then terminates with OutOfMemory you can experiment with lower value.
rem set FFDEC_STACK_SIZE=32m

rem Uncomment following when you want to disable checks for hardware acceleration compatibility.
rem set J2D_D3D_NO_HWCHECK=true

rem Hide VLC error output
set VLC_VERBOSE=-1

if "%FFDEC_MEMORY%"=="" set FFDEC_MEMORY=1024m

set STACK_SIZE_PARAM=
set MEMORY_PARAM=
if not "%FFDEC_STACK_SIZE%"=="" set STACK_SIZE_PARAM="-Xss%FFDEC_STACK_SIZE%" 
if not "%FFDEC_MEMORY%"=="" set MEMORY_PARAM="-Xmx%FFDEC_MEMORY%" 

java %MEMORY_PARAM% %STACK_SIZE_PARAM% -Djava.net.preferIPv4Stack=true -Djna.nosys=true -Djava.util.Arrays.useLegacyMergeSort=true -jar "%~dp0\ffdec.jar" %*
