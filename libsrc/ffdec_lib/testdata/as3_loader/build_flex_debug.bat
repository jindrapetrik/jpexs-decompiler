@echo off
java src_xor/Xor.java bin/inside.swf bin/inside_xored.swf
call c:\flex\bin\mxmlc.bat -debug=true -output bin/as3_loader.swf src/Main.as 1> buildlog.loader.txt 2>&1
rem -warnings=false
