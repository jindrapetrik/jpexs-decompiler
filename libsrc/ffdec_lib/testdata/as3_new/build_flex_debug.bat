@echo off
set COMPILERKIND=flex
set SWFNAME=as3_new
call c:\flex\bin\mxmlc.bat -debug=true -output bin/%SWFNAME%.%COMPILERKIND%.swf src/Main.as 1> buildlog.%COMPILERKIND%.txt 2>&1
rem -warnings=false
