@echo off
set COMPILERKIND=flex
set SWFNAME=as3_embed
c:\flex\bin\mxmlc.exe -debug=true -output bin/%SWFNAME%.%COMPILERKIND%.swf src/MainFlex.as 1> buildlog.%COMPILERKIND%.txt 2>&1
rem -warnings=false