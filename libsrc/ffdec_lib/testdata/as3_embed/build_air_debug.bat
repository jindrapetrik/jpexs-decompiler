@echo off
set COMPILERKIND=air
call c:\air\bin\mxmlc.bat -debug=true -output bin/as3_embed_attrib.%COMPILERKIND%.swf src/MainAttributesAir.as 1> buildlog.attrib.%COMPILERKIND%.txt 2>&1
call c:\air\bin\mxmlc.bat -debug=true -output bin/as3_embed_classes.%COMPILERKIND%.swf src/MainClassesAir.as 1> buildlog.classes.%COMPILERKIND%.txt 2>&1

rem -warnings=false
