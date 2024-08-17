@echo off

:: Set working dir
cd %~dp0 & cd ..

if not exist %CERT_FILE% goto certificate

:: AIR output
if not exist %AIR_PATH% md %AIR_PATH%
set OUTPUT=%AIR_PATH%\%AIR_NAME%%AIR_TARGET%.air

:: Package
echo.
echo Packaging %AIR_NAME%%AIR_TARGET%.air using certificate %CERT_FILE%...
call adt -package %OPTIONS% %SIGNING_OPTIONS% %OUTPUT% %APP_XML% %FILE_OR_DIR%
if errorlevel 1 goto failed
goto end

:certificate
echo.
echo Certificate not found: %CERT_FILE%
echo.
echo Troubleshooting: 
echo - generate a default certificate using 'bat\CreateCertificate.bat'
echo.
if %PAUSE_ERRORS%==1 pause
exit

:failed
echo AIR setup creation FAILED.
echo.
echo Troubleshooting: 
echo - verify AIR SDK target version in %APP_XML%
echo.
if %PAUSE_ERRORS%==1 pause
exit

:end
echo.
