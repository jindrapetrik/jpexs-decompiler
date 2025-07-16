@echo off
set COMPILERKIND=flex
set SWFNAME=as3_cross_compile
rem c:\flex\bin\mxmlc.exe -warnings=false -debug=true -output bin/%SWFNAME%.%COMPILERKIND%.swf src/Main.as 1> buildlog.%COMPILERKIND%.txt 2>&1
call c:\flex\bin\mxmlc.bat -warnings=false -debug=true -output bin/%SWFNAME%.%COMPILERKIND%.swf src/Main.as 1> buildlog.%COMPILERKIND%.txt 2>&1
IF NOT ERRORLEVEL 0 echo "FAILED"
exit /b 0