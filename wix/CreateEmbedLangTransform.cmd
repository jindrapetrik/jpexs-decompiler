set MsiName=%1
set BuildType=%2
set lang=%3
set langcode=%4

"%MsiToolsPath%\msitran.exe" -g bin\%BuildType%\en-US\%MsiName%.msi bin\%BuildType%\%lang%\%MsiName%.msi %langcode%
"%MsiToolsPath%\msidb.exe" -d bin\%BuildType%\%MsiName%.msi -r %langcode%
del %langcode%