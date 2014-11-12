; Version 1.0.0
; by JPEXS from JREDyna template
  
  !include "WordFunc.nsh"
  !include "x64.nsh"


  !macro CUSTOM_PAGE_FLASHINFO
    Page custom CUSTOM_PAGE_FLASHINFO
  !macroend

  !ifndef FLASH_URL
    !error "FLASH_URL must be defined"
  !endif


;;;;;;;;;;;;;;;;;;;;;
;  Custom panel
;;;;;;;;;;;;;;;;;;;;;

Function CUSTOM_PAGE_FLASHINFO
  
  SetRegView 32
  ClearErrors
  ReadRegStr $R0 HKCR "CLSID\{D27CDB6E-AE6D-11cf-96B8-444553540000}" ""
  IfErrors 0 exit     
  
  nsDialogs::create /NOUNLOAD 1018
  pop $1


  !insertmacro MUI_HEADER_TEXT "Flash Player ActiveX control required" "This application requires Flash ActiveX control"
  ${NSD_CreateLabel} 0 0 100% 100% "This application requires installation of the Flash ActiveX control. This will be downloaded and installed as part of the installation."
  pop $1
  goto ShowDialog

ShowDialog:

  nsDialogs::Show

exit:

FunctionEnd




Function DownloadAndInstallFlashIfNecessary
  Push $0
  Push $1

  DetailPrint "Detecting Flash ActiveX"
  SetRegView 32
  ClearErrors
  ReadRegStr $R0 HKCR "CLSID\{D27CDB6E-AE6D-11cf-96B8-444553540000}" ""
  IfErrors downloadFlash 
  DetailPrint "Flash ActiveX already installed"   
  Goto End

downloadFlash:
  DetailPrint "Result: Flash Active X is missing."
  DetailPrint "About to download Flash from ${FLASH_URL}"
  Inetc::get "${FLASH_URL}" "$TEMP\flash_ax_setup.exe" /END
  Pop $0 # return value = exit code, "OK" if OK
  DetailPrint "Download result = $0"

  strcmp $0 "OK" downloadsuccessful
  MessageBox MB_ABORTRETRYIGNORE|MB_ICONSTOP "Cannot download Flash ActiveX. You can download it later manually or use our own flash viewer." /SD IDIGNORE IDRETRY downloadFlash IDIGNORE End
  Abort

downloadsuccessful:


  DetailPrint "Launching Flash setup"
  
  IfSilent doSilent
  ExecWait '"$TEMP\flash_ax_setup.exe"' $0
  goto flashSetupfinished
doSilent:
  ExecWait '"$TEMP\flash_ax_setup.exe" -install' $0
  

flashSetupFinished:
  DetailPrint "Flash Setup finished"
  Delete "$TEMP\flash_ax_setup.exe"
  StrCmp $0 "0" InstallVerif 0
  Push "The Flash setup has been abnormally interrupted - return code $0"
  Goto ExitInstallFlash
 
InstallVerif:
  DetailPrint "Checking the Flash Setup's outcome"
  SetRegView 32
  ClearErrors
  ReadRegStr $R0 HKCR "CLSID\{D27CDB6E-AE6D-11cf-96B8-444553540000}" ""
  IfErrors 0 ExitInstallFlash 
  Push "Unable to find Flash ActiveX, even though the Flash setup was successful$\n$\n$1"
  Goto ExitInstallFlash
  
ExitInstallFlash:
  Pop $1
  MessageBox MB_ABORTRETRYIGNORE|MB_ICONSTOP "Unable to install Flash ActiveX. You can download it later manually or use our own flash viewer.\n$\n$1" /SD IDIGNORE IDRETRY downloadFlash IDIGNORE End
  Pop $1 	; Restore $1
  Pop $0 	; Restore $0
  Abort
End:
  Pop $1	; Restore $1
  Pop $0	; Restore $0

FunctionEnd

  