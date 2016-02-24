@echo off
set ISDEBUG=false
if "%1" == "debug" goto blockset
goto block2
:blockset
set ISDEBUG=true
:block2
set COMPILERPATH=%2
rem if not exist %COMPILERPATH% goto notex
%COMPILERPATH% -warnings=false -debug=%ISDEBUG% -static-link-runtime-shared-libraries TestFlex.as>out.txt
start notepad out.txt
if errorlevel==1 goto failed
goto end
:notex
echo Flex SDK not found. Download and unpack Flex SDK into some directory and add it to PATH variable
goto end
:failed
pause
:end
