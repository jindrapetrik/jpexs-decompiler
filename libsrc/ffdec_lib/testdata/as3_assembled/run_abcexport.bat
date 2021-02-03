@echo off
set RABCDASM_PATH=c:\RABCDasm
set SWFNAME=as3_assembled
rd /s /q .\abc
%RABCDASM_PATH%\abcexport.exe .\bin\%SWFNAME%.swf
mkdir .\abc
move .\bin\*.abc abc\>NUL
set ABC_DIR=.\abc

for /F %%x in ('dir /B/D %ABC_DIR%') do (   
   %RABCDASM_PATH%\rabcdasm.exe %ABC_DIR%\%%x
)

pause