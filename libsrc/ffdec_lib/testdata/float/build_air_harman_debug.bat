@echo off
set COMPILERKIND=air_harman
set SWFNAME=float
rem call c:\air_harman\bin\mxmlc.bat -debug=true -output bin/%SWFNAME%.%COMPILERKIND%.swf src/Main.as 1> buildlog.%COMPILERKIND%.txt 2>&1
set batdir=%~dp0
echo %batdir%bin\%SWFNAME%.%COMPILERKIND%.xml
cd c:\air_harman\bin
adl.exe -nodebug %batdir%bin\%SWFNAME%.%COMPILERKIND%.xml
rem -cmd
pause
