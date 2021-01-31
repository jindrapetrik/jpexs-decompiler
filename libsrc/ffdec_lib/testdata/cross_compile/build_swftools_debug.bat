@echo off
set COMPILERKIND=swftools
cd src
c:\swftools\as3compile.exe Main.as -o ..\bin\Main.%COMPILERKIND%.swf 1> ../buildlog.%COMPILERKIND%.txt 2>&1