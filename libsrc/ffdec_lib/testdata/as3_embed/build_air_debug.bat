@echo off
set COMPILERKIND=air
set SWFNAME=as3_embed
call c:\air\bin\mxmlc.bat -debug=true -output bin/%SWFNAME%.%COMPILERKIND%.swf src/MainAir.as 1> buildlog.%COMPILERKIND%.txt 2>&1
rem -warnings=false