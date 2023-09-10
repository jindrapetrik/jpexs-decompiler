@echo off
set COMPILERKIND=flex
c:\flex\bin\mxmlc.exe -debug=true -static-link-runtime-shared-libraries -output bin/as3_embed_attrib.%COMPILERKIND%.swf src/MainAttributesFlex.as 1> buildlog.attrib.%COMPILERKIND%.txt 2>&1
c:\flex\bin\mxmlc.exe -debug=true -static-link-runtime-shared-libraries -output bin/as3_embed_classes.%COMPILERKIND%.swf src/MainClassesFlex.as 1> buildlog.classes.%COMPILERKIND%.txt 2>&1

rem -warnings=false
