@echo off
set ISDEBUG=false
if "%1" == "debug" goto blockset
goto block2
:blockset
set ISDEBUG=true
:block2
set COMPILERPATH=%2
set COMPILERKIND=%3
rem if not exist %COMPILERPATH% goto notex
%COMPILERPATH% -warnings=false -debug=%ISDEBUG% -output bin/Main.%COMPILERKIND%.swf src/Main.as > buildlog.%COMPILERKIND%.txt
if errorlevel==1 goto failed
goto end
:notex
echo Flex/AIR SDK not found. Download and unpack Flex/AIR SDK into some directory and add it to PATH variable
goto end
:failed
pause
:end
