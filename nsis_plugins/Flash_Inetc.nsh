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


  !insertmacro MUI_HEADER_TEXT $(STRING_FLASH_AX_REQUIRED_TITLE) $(STRING_FLASH_AX_REQUIRED)  
  ${NSD_CreateLabel} 0 0 100% 100% $(STRING_FLASH_AX_WILLINSTALL)
  pop $1
  goto ShowDialog

ShowDialog:

  nsDialogs::Show

exit:

FunctionEnd




Function DownloadAndInstallFlashIfNecessary
  Push $0
  Push $1

  DetailPrint "$(STRING_FLASH_AX_DETECTING)"
  SetRegView 32
  ClearErrors
  ReadRegStr $R0 HKCR "CLSID\{D27CDB6E-AE6D-11cf-96B8-444553540000}" ""
  IfErrors downloadFlash 
  DetailPrint "$(STRING_FLASH_AX_ALREADYINSTALLED)"   
  Goto End

downloadFlash:
  DetailPrint "$(STRING_FLASH_AX_MISSING)"
  DetailPrint "$(STRING_FLASH_AX_WILLDOWNLOAD) ${FLASH_URL}"
  Inetc::get "${FLASH_URL}" "$TEMP\flash_ax_setup.exe" /END
  Pop $0 # return value = exit code, "OK" if OK
  DetailPrint "$(STRING_FLASH_AX_DOWNRESULT)$0"

  strcmp $0 "OK" downloadsuccessful
  MessageBox MB_ABORTRETRYIGNORE|MB_ICONSTOP "$(STRING_FLASH_AX_CANNOTDOWNLOAD)" /SD IDIGNORE IDRETRY downloadFlash IDIGNORE End
  Abort

downloadsuccessful:


  DetailPrint "$(STRING_FLASH_AX_LAUNCHSETUP)"
  
  IfSilent doSilent
  ExecWait '"$TEMP\flash_ax_setup.exe"' $0
  goto flashSetupfinished
doSilent:
  ExecWait '"$TEMP\flash_ax_setup.exe" -install' $0
  

flashSetupFinished:
  DetailPrint "$(STRING_FLASH_AX_SETUPFINISHED)"
  Delete "$TEMP\flash_ax_setup.exe"
  StrCmp $0 "0" InstallVerify 0
  Push "$(STRING_FLASH_AX_INTERRUPTED) $0"
  Goto ExitInstallFlash
 
InstallVerify:
  DetailPrint "$(STRING_FLASH_AX_SETUPOUTCOME)"
  SetRegView 32
  ClearErrors
  ReadRegStr $R0 HKCR "CLSID\{D27CDB6E-AE6D-11cf-96B8-444553540000}" ""
  IfErrors 0 ExitInstallFlash 
  Push "$(STRING_FLASH_AX_UNABLEFINDAFTER)$\n$\n$1"
  Goto ExitInstallFlash
  
ExitInstallFlash:
  Pop $1
  MessageBox MB_ABORTRETRYIGNORE|MB_ICONSTOP "$(STRING_FLASH_AX_UNABLEINSTALL)\n$\n$1" /SD IDIGNORE IDRETRY downloadFlash IDIGNORE End
  Pop $1 	; Restore $1
  Pop $0 	; Restore $0
  Abort
End:
  Pop $1	; Restore $1
  Pop $0	; Restore $0

FunctionEnd

  