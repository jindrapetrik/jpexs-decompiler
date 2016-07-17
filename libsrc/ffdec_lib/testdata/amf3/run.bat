@echo off
set ISDEBUG=false
if "%1" == "debug" goto blockset
goto block2
:blockset
set ISDEBUG=true
:block2
set AIRPATH=c:\air
set COMPILERPATH=%AIRPATH%\bin\amxmlc.bat
if not exist %COMPILERPATH% goto notex
call %COMPILERPATH% -warnings=false -debug=%ISDEBUG% AmfTest.as>NUL
if errorlevel==1 goto failed
goto end
:notex
echo AIR SDK not found. Download and unpack Flex SDK into C:\air directory, then run build again
goto end
:failed
pause
:end
%AIRPATH%\bin\adl.exe app.xml