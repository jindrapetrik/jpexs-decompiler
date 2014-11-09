;--------------------------------
;Include Modern UI

  !include "MUI2.nsh"

;--------------------------------
;General

;These are defined in Ant script: 
;!define APP_VER "4.0"
;!define APP_VER_MAJOR 4
;!define APP_VER_MINOR 0
;!define APP_URL "http://www.free-decompiler.com/flash/"
;!define APP_PUBLISHER "JPEXS"
;!define APP_NAME "JPEXS Free Flash Decompiler"
;!define JRE_VERSION "1.7"


!addplugindir "nsis_plugins\ansi\"
;!addplugindir "nsis_plugins\unicode\"


SetCompressor /SOLID lzma
!define JRE_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=52252"
!include "nsis_plugins\JREDyna_Inetc.nsh"

!define FLASH_URL "http://download.macromedia.com/pub/flashplayer/current/support/install_flash_player_ax.exe"
!include "nsis_plugins\Flash_Inetc.nsh"
!include x64.nsh


!define APP_SHORTVERNAME "JPEXS FFDec v. ${APP_VER}"

!define APP_VERNAME "${APP_NAME} v. ${APP_VER}"
!define MUI_WELCOMEFINISHPAGE_BITMAP "graphics\installer_164x314.bmp" 
!define MUI_HEADERIMAGE 
!define MUI_HEADERIMAGE_BITMAP "graphics\installer_150x57.bmp"

!define APP_UNINSTKEY "{E618D276-6596-41F4-8A98-447D442A77DB}_is1"





  ;Name and file
  Name "${APP_SHORTVERNAME}"
  OutFile "${APP_SETUPFILE}"



  ;Default installation folder
  InstallDir "$PROGRAMFILES\FFDec"
  
  ;Get installation folder from registry if available
  InstallDirRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "InstallLocation"

  ;Request application privileges for Windows Vista
  RequestExecutionLevel admin
 
;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING
  !define MUI_LANGDLL_ALLLANGUAGES


  
!define MUI_PAGE_CUSTOMFUNCTION_SHOW un.ModifyUnWelcome
!define MUI_PAGE_CUSTOMFUNCTION_LEAVE un.LeaveUnWelcome
!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_INSTFILES



!include LogicLib.nsh
 
; Return on top of stack the total size of the selected (installed) sections, formated as DWORD
; Assumes no more than 256 sections are defined
Var GetInstalledSize.total
Function GetInstalledSize
	Push $0
	Push $1
	StrCpy $GetInstalledSize.total 0
	${ForEach} $1 0 256 + 1
		${if} ${SectionIsSelected} $1
			SectionGetSize $1 $0
			IntOp $GetInstalledSize.total $GetInstalledSize.total + $0
		${Endif}
 
		; Error flag is set when an out-of-bound section is referenced
		${if} ${errors}
			${break}
		${Endif}
	${Next}
 
	ClearErrors
	Pop $1
	Pop $0
	IntFmt $GetInstalledSize.total "0x%08X" $GetInstalledSize.total
	Push $GetInstalledSize.total
FunctionEnd

 

!define StrLoc "!insertmacro StrLoc"
 
!macro StrLoc ResultVar String SubString StartPoint
  Push "${String}"
  Push "${SubString}"
  Push "${StartPoint}"
  Call StrLoc
  Pop "${ResultVar}"
!macroend
 
Function StrLoc
/*After this point:
  ------------------------------------------
   $R0 = StartPoint (input)
   $R1 = SubString (input)
   $R2 = String (input)
   $R3 = SubStringLen (temp)
   $R4 = StrLen (temp)
   $R5 = StartCharPos (temp)
   $R6 = TempStr (temp)*/
 
  ;Get input from user
  Exch $R0
  Exch
  Exch $R1
  Exch 2
  Exch $R2
  Push $R3
  Push $R4
  Push $R5
  Push $R6
 
  ;Get "String" and "SubString" length
  StrLen $R3 $R1
  StrLen $R4 $R2
  ;Start "StartCharPos" counter
  StrCpy $R5 0
 
  ;Loop until "SubString" is found or "String" reaches its end
  ${Do}
    ;Remove everything before and after the searched part ("TempStr")
    StrCpy $R6 $R2 $R3 $R5
 
    ;Compare "TempStr" with "SubString"
    ${If} $R6 == $R1
      ${If} $R0 == `<`
        IntOp $R6 $R3 + $R5
        IntOp $R0 $R4 - $R6
      ${Else}
        StrCpy $R0 $R5
      ${EndIf}
      ${ExitDo}
    ${EndIf}
    ;If not "SubString", this could be "String"'s end
    ${If} $R5 >= $R4
      StrCpy $R0 ``
      ${ExitDo}
    ${EndIf}
    ;If not, continue the loop
    IntOp $R5 $R5 + 1
  ${Loop}
 
  ;Return output to user
  Pop $R6
  Pop $R5
  Pop $R4
  Pop $R3
  Pop $R2
  Exch
  Pop $R1
  Exch $R0
FunctionEnd

Function GetTime
	!define GetTime `!insertmacro GetTimeCall`
 
	!macro GetTimeCall _FILE _OPTION _R1 _R2 _R3 _R4 _R5 _R6 _R7
		Push `${_FILE}`
		Push `${_OPTION}`
		Call GetTime
		Pop ${_R1}
		Pop ${_R2}
		Pop ${_R3}
		Pop ${_R4}
		Pop ${_R5}
		Pop ${_R6}
		Pop ${_R7}
	!macroend
 
	Exch $1
	Exch
	Exch $0
	Exch
	Push $2
	Push $3
	Push $4
	Push $5
	Push $6
	Push $7
	ClearErrors
 
	StrCmp $1 'L' gettime
	StrCmp $1 'A' getfile
	StrCmp $1 'C' getfile
	StrCmp $1 'M' getfile
	StrCmp $1 'LS' gettime
	StrCmp $1 'AS' getfile
	StrCmp $1 'CS' getfile
	StrCmp $1 'MS' getfile
	goto error
 
	getfile:
	IfFileExists $0 0 error
	System::Call /NOUNLOAD '*(i,l,l,l,i,i,i,i,&t260,&t14) i .r6'
	System::Call /NOUNLOAD 'kernel32::FindFirstFileA(t,i)i(r0,r6) .r2'
	System::Call /NOUNLOAD 'kernel32::FindClose(i)i(r2)'
 
	gettime:
	System::Call /NOUNLOAD '*(&i2,&i2,&i2,&i2,&i2,&i2,&i2,&i2) i .r7'
	StrCmp $1 'L' 0 systemtime
	System::Call /NOUNLOAD 'kernel32::GetLocalTime(i)i(r7)'
	goto convert
	systemtime:
	StrCmp $1 'LS' 0 filetime
	System::Call /NOUNLOAD 'kernel32::GetSystemTime(i)i(r7)'
	goto convert
 
	filetime:
	System::Call /NOUNLOAD '*$6(i,l,l,l,i,i,i,i,&t260,&t14)i(,.r4,.r3,.r2)'
	System::Free /NOUNLOAD $6
	StrCmp $1 'A' 0 +3
	StrCpy $2 $3
	goto tolocal
	StrCmp $1 'C' 0 +3
	StrCpy $2 $4
	goto tolocal
	StrCmp $1 'M' tolocal
 
	StrCmp $1 'AS' tosystem
	StrCmp $1 'CS' 0 +3
	StrCpy $3 $4
	goto tosystem
	StrCmp $1 'MS' 0 +3
	StrCpy $3 $2
	goto tosystem
 
	tolocal:
	System::Call /NOUNLOAD 'kernel32::FileTimeToLocalFileTime(*l,*l)i(r2,.r3)'
	tosystem:
	System::Call /NOUNLOAD 'kernel32::FileTimeToSystemTime(*l,i)i(r3,r7)'
 
	convert:
	System::Call /NOUNLOAD '*$7(&i2,&i2,&i2,&i2,&i2,&i2,&i2,&i2)i(.r5,.r6,.r4,.r0,.r3,.r2,.r1,)'
	System::Free $7
 
	IntCmp $0 9 0 0 +2
	StrCpy $0 '0$0'
	IntCmp $1 9 0 0 +2
	StrCpy $1 '0$1'
	IntCmp $2 9 0 0 +2
	StrCpy $2 '0$2'
	IntCmp $6 9 0 0 +2
	StrCpy $6 '0$6'
 
	StrCmp $4 0 0 +3
	StrCpy $4 Sunday
	goto end
	StrCmp $4 1 0 +3
	StrCpy $4 Monday
	goto end
	StrCmp $4 2 0 +3
	StrCpy $4 Tuesday
	goto end
	StrCmp $4 3 0 +3
	StrCpy $4 Wednesday
	goto end
	StrCmp $4 4 0 +3
	StrCpy $4 Thursday
	goto end
	StrCmp $4 5 0 +3
	StrCpy $4 Friday
	goto end
	StrCmp $4 6 0 error
	StrCpy $4 Saturday
	goto end
 
	error:
	SetErrors
	StrCpy $0 ''
	StrCpy $1 ''
	StrCpy $2 ''
	StrCpy $3 ''
	StrCpy $4 ''
	StrCpy $5 ''
	StrCpy $6 ''
 
	end:
	Pop $7
	Exch $6
	Exch
	Exch $5
	Exch 2
	Exch $4
	Exch 3
	Exch $3
	Exch 4
	Exch $2
	Exch 5
	Exch $1
	Exch 6
	Exch $0
FunctionEnd

Function RIndexOf
Exch $R0
Exch
Exch $R1
Push $R2
Push $R3
 
 StrCpy $R3 $R0
 StrCpy $R0 0
 IntOp $R0 $R0 + 1
  StrCpy $R2 $R3 1 -$R0
  StrCmp $R2 "" +2
  StrCmp $R2 $R1 +2 -3
 
 StrCpy $R0 -1
 
Pop $R3
Pop $R2
Pop $R1

Exch $R0
FunctionEnd
 
!macro StrRPos Var Str Char
Push "${Char}"
Push "${Str}"
Call RIndexOf
Pop $1
StrLen $0 ${Str} 
IntOp ${Var} $0 - $1
!macroend


!define StrRPos "!insertmacro StrRPos"

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_LICENSE "resources/license.txt"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro CUSTOM_PAGE_JREINFO
  !insertmacro CUSTOM_PAGE_FLASHINFO

var SMDir

!define MUI_STARTMENUPAGE_DEFAULTFOLDER "${APP_NAME}"
!define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKLM" 
!define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Inno Setup: Icon Group"
  !insertmacro MUI_PAGE_STARTMENU 0 $SMDir
  !insertmacro MUI_PAGE_INSTFILES
  
!define MUI_FINISHPAGE_RUN "$INSTDIR\ffdec.exe"
  !insertmacro MUI_PAGE_FINISH


  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES

;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"
  !insertmacro MUI_LANGUAGE "Czech"  
  !insertmacro MUI_LANGUAGE "SimpChinese"
  !insertmacro MUI_LANGUAGE "Dutch"
  !insertmacro MUI_LANGUAGE "French"
  !insertmacro MUI_LANGUAGE "German"
  !insertmacro MUI_LANGUAGE "Hungarian"
  !insertmacro MUI_LANGUAGE "Polish"
  !insertmacro MUI_LANGUAGE "Portuguese"
  !insertmacro MUI_LANGUAGE "PortugueseBR"
  !insertmacro MUI_LANGUAGE "Russian"
  !insertmacro MUI_LANGUAGE "Spanish"
  !insertmacro MUI_LANGUAGE "Swedish"
  !insertmacro MUI_LANGUAGE "Ukrainian"



;--------------------------------
;Installer Sections



LangString Sec3PlayerGlobal ${LANG_ENGLISH} "PlayerGlobal.swc (download)"
LangString Sec3PlayerGlobal ${LANG_CZECH} "PlayerGlobal.swc (sta�en�)"



Section "FFDec" SecDummy

  SetShellVarContext all

  SetOutPath "$INSTDIR"
  
  File "dist\ffdec.exe"
  File "dist\ffdec.bat"
  File "dist\ffdec.jar"
  File "dist\license.txt"
  
  SetOutPath "$INSTDIR"  
  File /r "dist\lib"

 
 ;create start-menu items
!insertmacro MUI_STARTMENU_WRITE_BEGIN 0 ;This macro sets $SMDir and skips to MUI_STARTMENU_WRITE_END if the "Don't create shortcuts" checkbox is checked... 

  CreateDirectory "$SMPROGRAMS\$SMDir"
  CreateShortCut "$SMPROGRAMS\$SMDir\Uninstall ${APP_NAME}.lnk" "$INSTDIR\Uninstall.exe" "" "$INSTDIR\Uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\$SMDir\${APP_NAME}.lnk" "$INSTDIR\ffdec.exe" "" "$INSTDIR\ffdec.exe" 0
 !insertmacro MUI_STARTMENU_WRITE_END

  ;Store installation folder

  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}"  
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "DisplayName" "${APP_NAME}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "UninstallString" '"$INSTDIR\Uninstall.exe"'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "QuietUninstallString" '"$INSTDIR\Uninstall.exe" /S'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "DisplayIcon" '"$INSTDIR\ffdec.exe"'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "DisplayVersion" "${APP_VER}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "URLInfoAbout" "${APP_URL}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "URLUpdateInfo" "${APP_URL}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "HelpLink" "${APP_URL}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "Publisher" "${APP_PUBLISHER}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "InstallLocation" "$INSTDIR"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "Inno Setup: Icon Group" "$SMDir"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "NSIS: Language" "$language"

  

  Call GetInstalledSize
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "EstimatedSize" $GetInstalledSize.total

  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "MajorVersion" ${APP_VER_MAJOR}
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "MinorVersion" ${APP_VER_MINOR}

  ${GetTime} "" "L" $0 $1 $2 $3 $4 $5 $6
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "InstallDate" "$2$1$0"

  ;Create un1installer
  WriteUninstaller "$INSTDIR\Uninstall.exe"

  call DownloadAndInstallJREIfNecessary
  call DownloadAndInstallFlashIfNecessary

SectionEnd




var txt
var pos
var pgfound
var f
var pgname

Section "PlayerGlobal.swc" SecPlayerGlobal

!tempfile PGHTML
inetc::get /SILENT /USERAGENT "${APP_NAME} Setup" https://www.adobe.com/support/flashplayer/downloads.html ${PGHTML}
Pop $0
StrCmp $0 "OK" dlok
MessageBox MB_OK|MB_ICONEXCLAMATION "PlayerGlobal.SWC was not found on Adobe webpages. Click OK to abort installation" /SD IDOK
Abort
dlok:

StrCpy $pgfound 0

FileOpen $f ${PGHTML} r
loop:
  FileRead $f $txt
  IfErrors done      
  StrCmp $pgfound 1 0 nolicheck
    ${StrLoc} $pos $txt "<li><a href=$\"" ">"
    StrCmp $pos "" nolicheck
      IntOp $pos $pos + 13
      StrCpy $txt $txt "" $pos
      ${StrLoc} $pos $txt "$\"" ">"
      StrCpy $txt $txt $pos
      StrCpy $pgfound 2   
      Goto done    
  nolicheck:
  ${StrLoc} $pos $txt "PlayerGlobal" ">"
  StrCmp $pos "" loop
    StrCpy $pgfound 1              
  Goto loop
done:
  FileClose $f
  StrCmp $pgfound 2 +3
    MessageBox MB_OK|MB_ICONEXCLAMATION "PlayerGlobal.SWC not found on Adobe Webpages, click OK to abort installation" /SD IDOK    
    Abort

  ${StrRPos} $pos $txt "/"
  IntOp $pos $pos + 1
  StrCpy $pgname $txt "" $pos    
  SetShellVarContext current    
  
  CreateDirectory "$APPDATA\JPEXS\FFDec\flashlib"
  inetc::get /USERAGENT "${APP_NAME} Setup" $txt "$APPDATA\JPEXS\FFDec\flashlib\$pgname"
SectionEnd

Section "Desktop Shortcut"
SetShellVarContext all
CreateShortCut "$DESKTOP\${APP_NAME}.lnk" "$INSTDIR\ffdec.exe" ""
SectionEnd

Function .onInit
  !insertmacro MUI_LANGDLL_DISPLAY
  IntOp $0 ${SF_SELECTED} | ${SF_RO}
  SectionSetFlags ${SecDummy} $0  
FunctionEnd

;--------------------------------
;Descriptions

  ;Language strings
  ;LangString DESC_SecDummy ${LANG_ENGLISH} "Application GUI and Libraries"
  ;LangString DESC_SecDummy ${LANG_CZECH} "Aplika�n� rozhran� a knihovny"

  ;LangString DESC_PlayerGlobal ${LANG_ENGLISH} "Download FlashPlayer library from Adobe site - useful for ActionScript direct editation and other features"
  ;LangString DESC_PlayerGlobal ${LANG_CZECH} "St�hnout knihovnu FlashPlayeru ze str�nek Adobe - u�ite�n� pro p��mou editaci ActionScriptu a dal�� v�ci"

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecDummy} "Application GUI and Libraries"
    !insertmacro MUI_DESCRIPTION_TEXT ${SecPlayerGlobal} "Download FlashPlayer library from Adobe site - useful for ActionScript direct editation and other features"   
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

         
;LangString DESC_UninstLocal ${LANG_ENGLISH} "Remove user configuration"
;LangString DESC_UninstLocal ${LANG_CZECH} "Odstranit u�ivatelskou konfiguraci"         




Var mycheckbox
Var uninstlocal

Function un.ModifyUnWelcome
${NSD_CreateCheckbox} 120u -18u 50% 12u "Remove user configuration"
Pop $mycheckbox
SetCtlColors $mycheckbox "" ${MUI_BGCOLOR}
;${NSD_Check} $mycheckbox ; Check it by default
FunctionEnd

Function un.LeaveUnWelcome
StrCpy $uninstlocal 0
${NSD_GetState} $mycheckbox $0
${If} $0 <> 0
    StrCpy $uninstlocal 1     
${EndIf}
FunctionEnd

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  SetShellVarContext all
  RMDir /r "$INSTDIR\*.*"    
 
  Delete "$INSTDIR\Uninstall.exe"

  RMDir "$INSTDIR"

 ;Delete Start Menu Shortcuts
  Delete "$DESKTOP\${APP_NAME}.lnk"
  

  !insertmacro MUI_STARTMENU_GETFOLDER 0 $SMDir  

  RmDir /r "$SMPROGRAMS\$SMDir\*.*"
  RmDir "$SMPROGRAMS\$SMDir"   
 
  StrCmp $uninstlocal 1 0 +5
    SetShellVarContext current      
    RmDir /r "$APPDATA\JPEXS\FFDec\*.*"
    RmDir "$APPDATA\JPEXS\FFDec"
    RmDir "$APPDATA\JPEXS"

  DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}"  

SectionEnd


;TODO: FlashPlayer detection/install, Java detection/install