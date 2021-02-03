@echo off
set COMPILERKIND=flex
set SWFNAME=as3_new
c:\flex\bin\mxmlc.exe -warnings=false -debug=true -output bin/%SWFNAME%.%COMPILERKIND%.swf src/Main.as 1> buildlog.%COMPILERKIND%.txt 2>&1