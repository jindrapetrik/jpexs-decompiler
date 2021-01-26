@echo off
set RABCDASM_PATH=c:\RABCDasm
%RABCDASM_PATH%\rabcasm.exe abc\custom-0\custom-0.main.asasm
%RABCDASM_PATH%\abcreplace.exe bin\custom.swf 0 abc\custom-0\custom-0.main.abc
pause