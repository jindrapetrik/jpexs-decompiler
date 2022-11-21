@echo off
rem Set following to higher value if you want more memory:
rem You need 64 bit OS and 64 bit java to set it to higher values
set MEMORY=1024m
java -Xmx%MEMORY% -Djna.nosys=true -jar "%~dp0\ffdec.jar" %*