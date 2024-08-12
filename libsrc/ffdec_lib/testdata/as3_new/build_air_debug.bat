@echo off
set COMPILERKIND=air
set SWFNAME=as3_new
call c:\air\bin\mxmlc.bat -debug=true -output bin/%SWFNAME%.%COMPILERKIND%.swf src/Main.as 1> buildlog.%COMPILERKIND%.txt 2>&1
rem set COMPILERKIND=air.optimize
rem call c:\air\bin\mxmlc.bat -compiler.optimize -output bin/%SWFNAME%.%COMPILERKIND%.swf src/Main.as 1> buildlog.%COMPILERKIND%.txt 2>&1
rem -warnings=false
