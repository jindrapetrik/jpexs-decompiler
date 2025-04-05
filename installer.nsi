;--------------------------------
;Include Modern UI

  !include "MUI2.nsh"

;--------------------------------
;General

;These are defined in Ant script:
!ifndef APP_VER 
  !define APP_VER "0.0"
  !define APP_VER_MAJOR 0
  !define APP_VER_MINOR 0
  !define APP_URL "https://github.com/jindrapetrik/jpexs-decompiler"
  !define APP_PUBLISHER "JPEXS"
  !define APP_NAME "JPEXS Free Flash Decompiler"
;  !define JRE_VERSION "1.8"
!endif

Unicode true


!define APP_EXENAME "ffdec.exe"

SetCompressor /SOLID lzma
!include "StrFunc.nsh"
!include x64.nsh


!define APP_SHORTVERNAME "JPEXS FFDec v. ${APP_VER}"

!define APP_VERNAME "${APP_NAME} v. ${APP_VER}"
!define MUI_WELCOMEFINISHPAGE_BITMAP "graphics\installer2_164x314.bmp" 
!define MUI_HEADERIMAGE 
!define MUI_HEADERIMAGE_BITMAP "graphics\installer2_150x57.bmp"

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
  ;!insertmacro CUSTOM_PAGE_JREINFO
  ;!insertmacro CUSTOM_PAGE_FLASHINFO

var SMDir

!define MUI_STARTMENUPAGE_DEFAULTFOLDER "${APP_NAME}"
!define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKLM" 
!define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Inno Setup: Icon Group"
  !insertmacro MUI_PAGE_STARTMENU 0 $SMDir
  ;Page custom CUSTOM_PAGE_CONTEXTMENU
  !insertmacro MUI_PAGE_INSTFILES
  ;Page custom CUSTOM_PAGE_HELPUS
!define MUI_FINISHPAGE_RUN "$INSTDIR\${APP_EXENAME}"
  !insertmacro MUI_PAGE_FINISH


  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES

;--------------------------------
;Languages

!define !IfExist `!insertmacro _!IfExist ""`

!define !IfNExist `!insertmacro _!IfExist "n"`
    !macro _!IfExist _OP _FilePath
        !ifdef !IfExistIsTrue
            !undef !IfExistIsTrue
        !endif
        !tempfile "!IfExistTmp"
        !system `IF EXIST "${_FilePath}" Echo !define "!IfExistIsTrue" > "${!IfExistTmp}"`
        !include /NONFATAL "${!IfExistTmp}"
        !delfile /NONFATAL "${!IfExistTmp}"
        !undef !IfExistTmp
        !if${_OP}def !IfExistIsTrue
    !macroend

!macro LANG_LOAD LANGLOAD
  !insertmacro MUI_LANGUAGE "${LANGLOAD}"
  !verbose push
  !verbose 0
  !include "nsis_locales\${LANGLOAD}.nsh"
  !verbose pop
  !undef LANG
!macroend
 
!macro LANG_STRING NAME VALUE
  LangString "${NAME}" ${LANG_${LANG}} "${VALUE}"
!macroend
 
!macro LANG_UNSTRING NAME VALUE
  !insertmacro LANG_STRING "un.${NAME}" "${VALUE}"
!macroend


 
  !insertmacro LANG_LOAD "English"
  !insertmacro LANG_LOAD "Catalan"    
  !insertmacro LANG_LOAD "Czech"  
  !insertmacro LANG_LOAD "SimpChinese"
  !insertmacro LANG_LOAD "Dutch"
  !insertmacro LANG_LOAD "French"
  !insertmacro LANG_LOAD "German"
  !insertmacro LANG_LOAD "Hungarian"
  !insertmacro LANG_LOAD "Polish"
  !insertmacro LANG_LOAD "Portuguese"
  !insertmacro LANG_LOAD "PortugueseBR"
  !insertmacro LANG_LOAD "Russian"
  !insertmacro LANG_LOAD "Spanish"
  !insertmacro LANG_LOAD "Swedish"
  !insertmacro LANG_LOAD "Turkish"  
  !insertmacro LANG_LOAD "Ukrainian"
  !insertmacro LANG_LOAD "Italian"


;--------------------------------
;Installer Sections

!macro IfKeyExists ROOT MAIN_KEY KEY
  Push $R0
  Push $R1
  Push $R2

  # XXX bug if ${ROOT}, ${MAIN_KEY} or ${KEY} use $R0 or $R1

  StrCpy $R1 "0" # loop index
  StrCpy $R2 "0" # not found

  ${Do}
    EnumRegKey $R0 ${ROOT} "${MAIN_KEY}" "$R1"
    ${If} $R0 == "${KEY}"
      StrCpy $R2 "1" # found
      ${Break}
    ${EndIf}
    IntOp $R1 $R1 + 1
  ${LoopWhile} $R0 != ""

  ClearErrors

  Exch 2
  Pop $R0
  Pop $R1
  Exch $R2
!macroend


!define REG_CLASSES_HKEY HKLM


Section "FFDec" SecDummy
                                      
  SetShellVarContext all

  SetOutPath "$INSTDIR"
  
  File "dist\${APP_EXENAME}"
  File "dist\ffdec.bat"
  File "dist\ffdec.jar"
  File "dist\ffdec-cli.exe"
  File "dist\ffdec-cli.jar"
  File "dist\icon.ico"
  File "dist\license.txt"
  File "dist\soleditor.bat"
  ;File "dist\soleditor.lnk"
  File "dist\translator.bat"
  ;File "dist\translator.lnk"
  
  SetOutPath "$INSTDIR"  
  File /r "dist\flashlib"
  File /r "dist\lib"

 ;create start-menu items
!insertmacro MUI_STARTMENU_WRITE_BEGIN 0 ;This macro sets $SMDir and skips to MUI_STARTMENU_WRITE_END if the "Don't create shortcuts" checkbox is checked... 

  CreateDirectory "$SMPROGRAMS\$SMDir"
  CreateShortCut "$SMPROGRAMS\$SMDir\Uninstall ${APP_NAME}.lnk" "$INSTDIR\Uninstall.exe" "" "$INSTDIR\Uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\$SMDir\${APP_NAME}.lnk" "$INSTDIR\${APP_EXENAME}" "" "$INSTDIR\${APP_EXENAME}" 0
  CreateShortCut "$SMPROGRAMS\$SMDir\$(STRING_SOL_EDITOR).lnk" "$INSTDIR\${APP_EXENAME}" "-soleditor" "$INSTDIR\${APP_EXENAME}" 2
 !insertmacro MUI_STARTMENU_WRITE_END

  ;Store installation folder

  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}"  
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "DisplayName" "${APP_NAME}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "UninstallString" '"$INSTDIR\Uninstall.exe"'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "QuietUninstallString" '"$INSTDIR\Uninstall.exe" /S'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}" "DisplayIcon" '"$INSTDIR\${APP_EXENAME}"'
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

  ;call DownloadAndInstallJREIfNecessary
  ;call DownloadAndInstallFlashIfNecessary

SectionEnd

Section $(STRING_DESKTOP_SHORTCUT) SecShortcut
SetShellVarContext all
CreateShortCut "$DESKTOP\${APP_NAME}.lnk" "$INSTDIR\${APP_EXENAME}" ""
SectionEnd

Function .onInit
  !insertmacro MUI_LANGDLL_DISPLAY
  IntOp $0 ${SF_SELECTED} | ${SF_RO}
  SectionSetFlags ${SecDummy} $0  
FunctionEnd

;--------------------------------
;Descriptions

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecDummy} "$(STRING_SECTION_APP)"
 ;   !insertmacro MUI_DESCRIPTION_TEXT ${SecContextMenu} "$(STRING_SECTION_CONTEXT_MENU)"
    !insertmacro MUI_DESCRIPTION_TEXT ${SecShortcut} "$(STRING_SECTION_SHORTCUT)"
  !insertmacro MUI_FUNCTION_DESCRIPTION_END




Var mycheckbox
Var uninstlocal

Function un.ModifyUnWelcome
${NSD_CreateCheckbox} 120u -18u 50% 12u "$(STRING_UNINST_USER)"
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

  DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}"

SectionEnd
