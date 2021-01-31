@echo off
set COMPILERKIND=air
call c:\air\bin\mxmlc.bat -warnings=false -debug=true -output bin/Main.%COMPILERKIND%.swf src/Main.as 1> buildlog.%COMPILERKIND%.txt 2>&1