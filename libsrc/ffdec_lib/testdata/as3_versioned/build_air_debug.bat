@echo off
set COMPILERKIND=flex
set SWFNAME=as3_versioned
rem call c:\air\bin\mxmlc.bat -debug=true -builtin -apiversioning -output bin/%SWFNAME%.%COMPILERKIND%.swf src/Main.as 1> buildlog.%COMPILERKIND%.txt 2>&1
java -classpath c:\flex\lib\asc.jar macromedia.asc.embedding.ScriptCompiler -optimize -builtin -apiversioning -outdir bin/ -out as3_versioned src/stubs.as src/Toplevel.as
pause
rem -warnings=false
