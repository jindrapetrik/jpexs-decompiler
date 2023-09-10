@echo off
set COMPILERKIND=air
call c:\air\bin\mxmlc.bat -debug=true -static-link-runtime-shared-libraries -output bin/as3_embed_attrib.%COMPILERKIND%.swf src/MainAttributesAir.as 1> buildlog.attrib.%COMPILERKIND%.txt 2>&1
call c:\air\bin\mxmlc.bat -debug=true -static-link-runtime-shared-libraries -output bin/as3_embed_classes.%COMPILERKIND%.swf src/MainClassesAir.as 1> buildlog.classes.%COMPILERKIND%.txt 2>&1

rem -warnings=false
