java -cp c:\flex\lib\asc.jar macromedia.asc.embedding.ScriptCompiler -strict -builtin -outdir bin/ -out compound src/stubs.as src/compound.as>buildlog.flex.txt 2>&1
rem call c:\flex\bin\mxmlc.bat -debug=true -output bin/compound.swf src/compound.as 1> buildlog.flex.txt 2>&1
