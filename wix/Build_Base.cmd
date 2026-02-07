set MsiName=FFDec
set MsiToolsPath=%1
set BuildType=%2
pwsh -File GenerateExtensions.ps1
rd /s /q bin
rd /s /q obj
dotnet build %MsiName%.wixproj -c %BuildType%
if errorlevel 1 exit /b %errorlevel%

if exist %MsiName%.msi del %MsiName%.msi
copy bin\%BuildType%\en-US\%MsiName%.msi bin\%BuildType%\%MSiName%.msi
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% cs-CZ 1029
goto after
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% ca-ES 1027
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% de-DE 1031
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% es-ES 1034
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% fr-FR 1036
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% hu-HU 1038
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% it-IT 1040
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% ja-JP 1041
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% nl-NL 1043
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% pl-PL 1045
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% pt-BR 1046
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% pt-PT 2070
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% ru-RU 1049
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% sk-SK 1051
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% sl-SI 1060
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% sv-SE 1053
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% tr-TR 1055
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% uk-UA 1058
call CreateEmbedLangTransform.cmd "%MsiToolsPath%" %MsiName% %BuildType% zh-CN 2052
:after
"%MsiToolsPath%\msiinfo.exe" bin\%BuildType%\%MsiName%.msi /p Intel;1033,1027,1029,1031,1034,1036,1038,1040,1041,1043,1045,1046,2070,1049,1051,1060,1053,1055,1058,2052 /t "JPEXS Free Flash Decompiler"


