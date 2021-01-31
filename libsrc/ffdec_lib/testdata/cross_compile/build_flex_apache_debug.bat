@echo off
set COMPILERKIND=flex_apache
call c:\flex_apache\bin\mxmlc.bat -warnings=false -debug=true -output bin/Main.%COMPILERKIND%.swf src/Main.as 1> buildlog.%COMPILERKIND%.txt 2>&1