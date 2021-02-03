@echo off
set COMPILERKIND=swftools
set SWFNAME=as3_cross_compile
cd src
c:\swftools\as3compile.exe Main.as -o ..\bin\%SWFNAME%.%COMPILERKIND%.swf 1> ../buildlog.%COMPILERKIND%.txt 2>&1