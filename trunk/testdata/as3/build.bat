@echo off
set ISDEBUG=false
if "%1" == "debug" goto blockset
goto block2
:blockset
set ISDEBUG=true
:block2
set COMPILERPATH=..\..\flex_sdk\bin\mxmlc.exe
if not exist %COMPILERPATH% goto notex
%COMPILERPATH% -warnings=false -debug=%ISDEBUG% TestMovie.as>out.txt
start notepad out.txt
if errorlevel==1 goto failed
goto end
:notex
echo Flex SDK not found. Download and unpack Flex SDK into trunk\flex_sdk directory, then run build again
goto end
:failed
pause
:end