@echo off

:: Set working dir
cd %~dp0 & cd ..

set PAUSE_ERRORS=1
call bat\SetupSDK.bat
call bat\SetupApp.bat

set AIR_TARGET=
::set AIR_TARGET=-captive-runtime
set OPTIONS=-tsa none
call bat\Packager.bat

pause
