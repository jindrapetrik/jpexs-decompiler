@echo off
set RABCDASM_PATH=c:\RABCDasm
set SWFNAME=as3_assembled
%RABCDASM_PATH%\rabcasm.exe abc\%SWFNAME%-0\%SWFNAME%-0.main.asasm
%RABCDASM_PATH%\abcreplace.exe bin\%SWFNAME%.swf 0 abc\%SWFNAME%-0\%SWFNAME%-0.main.abc
pause