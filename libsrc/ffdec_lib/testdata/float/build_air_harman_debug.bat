@echo off
set COMPILERKIND=air_harman
set SWFNAME=float
call c:\air_harman\bin\mxmlc.bat -debug=true -output bin/%SWFNAME%.%COMPILERKIND%.swf src/Main.as 1> buildlog.%COMPILERKIND%.txt 2>&1
c:\air_harman\bin\adl.exe -nodebug application.xml
