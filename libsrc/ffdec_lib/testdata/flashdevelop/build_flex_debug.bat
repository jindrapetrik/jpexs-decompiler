@echo off
set COMPILERKIND=flex
c:\flex\bin\mxmlc.exe -warnings=false -debug=true -output bin/Main.%COMPILERKIND%.swf src/Main.as 1> buildlog.%COMPILERKIND%.txt 2>&1