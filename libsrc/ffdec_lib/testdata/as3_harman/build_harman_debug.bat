@echo off
set MY_PATH=%~dp0
call c:\air_harman\bin\mxmlc.bat -debug=true -output bin/harman.swf src/Main.as 1> buildlog.harman.txt 2>&1
cd c:\air_harman\bin\
call swfencrypt.bat -in %MY_PATH%\bin\harman.swf -out %MY_PATH%\bin\harman_encrypted.swf
c:\air_harman\bin\adl.exe -nodebug %MY_PATH%\application.xml
