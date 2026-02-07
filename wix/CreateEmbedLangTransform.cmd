set MsiToolsPath=%1
set MsiName=%2
set BuildType=%3
set lang=%4
set langcode=%5

"%MsiToolsPath%\msitran.exe" -g bin\%BuildType%\en-US\%MsiName%.msi bin\%BuildType%\%lang%\%MsiName%.msi %langcode%
"%MsiToolsPath%\msidb.exe" -d bin\%BuildType%\%MsiName%.msi -r %langcode%
del %langcode%