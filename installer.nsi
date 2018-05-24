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
  !define JRE_VERSION "1.8"
!endif

Unicode true


!define APP_EXENAME "ffdec.exe"

;!addplugindir "nsis_plugins\ansi\"
!addplugindir "nsis_plugins\unicode\"


SetCompressor /SOLID lzma
!include "StrFunc.nsh"
${StrLoc}
!include "nsis_plugins\JREDyna_Inetc.nsh"

;Old not working
;!define FLASH_URL "http://download.macromedia.com/pub/flashplayer/current/support/install_flash_player_ax.exe"

!define FLASH_URL "http://fpdownload.macromedia.com/pub/flashplayer/latest/help/install_flash_player_ax.exe"

!include "nsis_plugins\Flash_Inetc.nsh"
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

!define un.StrRep "!insertmacro un.StrRep"
!macro un.StrRep output string old new
    Push `${string}`
    Push `${old}`
    Push `${new}`
    Call un.StrRep
    
    Pop ${output}
!macroend


!macro Func_StrRep un
    Function ${un}StrRep
        Exch $R2 ;new
        Exch 1
        Exch $R1 ;old
        Exch 2
        Exch $R0 ;string
        Push $R3
        Push $R4
        Push $R5
        Push $R6
        Push $R7
        Push $R8
        Push $R9

        StrCpy $R3 0
        StrLen $R4 $R1
        StrLen $R6 $R0
        StrLen $R9 $R2
        loop:
            StrCpy $R5 $R0 $R4 $R3
            StrCmp $R5 $R1 found
            StrCmp $R3 $R6 done
            IntOp $R3 $R3 + 1 ;move offset by 1 to check the next character
            Goto loop
        found:
            StrCpy $R5 $R0 $R3
            IntOp $R8 $R3 + $R4
            StrCpy $R7 $R0 "" $R8
            StrCpy $R0 $R5$R2$R7
            StrLen $R6 $R0
            IntOp $R3 $R3 + $R9 ;move offset by length of the replacement string
            Goto loop
        done:

        Pop $R9
        Pop $R8
        Pop $R7
        Pop $R6
        Pop $R5
        Pop $R4
        Pop $R3
        Push $R0
        Push $R1
        Pop $R0
        Pop $R1
        Pop $R0
        Pop $R2
        Exch $R1
    FunctionEnd
!macroend
;!insertmacro Func_StrRep ""
!insertmacro Func_StrRep "un."

;var AddToContextMenu
/*
Function CUSTOM_PAGE_CONTEXTMENU
  StrCpy $AddToContextMenu 1
  nsDialogs::create /NOUNLOAD 1018
  pop $1
  !insertmacro MUI_HEADER_TEXT "Add to Context Menu" "Set up Context menu"
  ${NSD_CreateLabel} 0 0 100% 50 "You can add FFDec to right click context menu in Windows Explorer."
  pop $1
  ${NSD_CreateCheckbox} 0 50 100% 25 "Add FFDec to context menu of SWF and GFX files"
  pop $1
  ${NSD_SetState} $1 ${BST_CHECKED}
  ${NSD_OnClick} $1 AddContextClick
  nsDialogs::Show
FunctionEnd

*/

/*
Function AddContextClick
  pop $1
  ${NSD_GetState} $1 $AddToContextMenu
FunctionEnd
*/

Function IndexOf
Exch $R0
Exch
Exch $R1
Push $R2
Push $R3

 StrCpy $R3 $R0
 StrCpy $R0 -1
 IntOp $R0 $R0 + 1
  StrCpy $R2 $R3 1 $R0
  StrCmp $R2 "" +2
  StrCmp $R2 $R1 +2 -3

 StrCpy $R0 -1

Pop $R3
Pop $R2
Pop $R1
Exch $R0
FunctionEnd

!macro IndexOf Var Str Char
Push "${Char}"
Push "${Str}"
 Call IndexOf
Pop "${Var}"
!macroend
!define IndexOf "!insertmacro IndexOf"

var clsname
!define VERB "ffdec"
!define VERBNAME "Open with FFDec"
!define ALPHABET "abcdefghijklmnopqrstuvwxyz"
var ext
var MRUList
var exists


!define REG_CLASSES_HKEY HKLM

Function un.RemoveExtContextMenu
  pop $ext
  DeleteRegKey ${REG_CLASSES_HKEY} "Software\Classes\Applications\${APP_EXENAME}"
  ReadRegStr $clsname ${REG_CLASSES_HKEY} "Software\Classes\.$ext" ""
  IfErrors step2
    DeleteRegKey ${REG_CLASSES_HKEY} "Software\Classes\$clsname\shell\${VERB}"
  step2:
  ReadRegStr $MRUList HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\$ext\OpenWithList" "MRUList"
     IfErrors step3
       StrLen $0 $MRUList
       ${For} $R1 0 $0
              StrCpy $2 $MRUList 1 $R1 ;Copy one character
              ReadRegStr $3 HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\$ext\OpenWithList" $2
              ${If} $3 == ${APP_EXENAME}
                ${un.StrRep} $MRUList $MRUList $2 ""
                WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\$ext\OpenWithList" "MRUList" $MRUList
                DeleteRegValue HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\$ext\OpenWithList" $2
                ${Break}
              ${EndIf}
       ${Next}
   step3:
  DeleteRegKey ${REG_CLASSES_HKEY} "Software\Classes\SystemFileAssociations\.$ext\Shell\${VERB}"
FunctionEnd




Function AddToExtContextMenu
    pop $ext
    
    
    WriteRegStr ${REG_CLASSES_HKEY} "Software\Classes\Applications\${APP_EXENAME}\shell\open" "" ${VERB}
    WriteRegStr ${REG_CLASSES_HKEY} "Software\Classes\Applications\${APP_EXENAME}\shell\open\command" "" '"$INSTDIR\${APP_EXENAME}" "%1"'

    !insertmacro IfKeyExists ${REG_CLASSES_HKEY} "Software\Classes" ".$ext"
     Pop $R0
     ${If} $R0 == 0
           WriteRegStr ${REG_CLASSES_HKEY} "Software\Classes\.$ext" "" "ShockwaveFlash.ShockwaveFlash"
     ${EndIf}

     ReadRegStr $clsname ${REG_CLASSES_HKEY} "Software\Classes\.$ext" ""
     !insertmacro IfKeyExists ${REG_CLASSES_HKEY} "Software\Classes" $clsname
     Pop $R0
     ${If} $R0 == 0
          WriteRegStr ${REG_CLASSES_HKEY} "Software\Classes\$clsname" "" "Flash Movie"
     ${EndIf}

     WriteRegStr ${REG_CLASSES_HKEY} "Software\Classes\$clsname\shell\${VERB}" "" "${VERBNAME}"
     WriteRegStr ${REG_CLASSES_HKEY} "Software\Classes\$clsname\shell\${VERB}\command" "" '"$INSTDIR\${APP_EXENAME}" "%1"'
    



     ReadRegStr $MRUList HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\$ext\OpenWithList" "MRUList"
     IfErrors not_mru
       StrLen $0 $MRUList
       StrCpy $exists 0
       ${For} $R1 0 $0
              StrCpy $2 $MRUList 1 $R1 ;Copy one character
              ReadRegStr $2 HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\$ext\OpenWithList" $2
              ${If} $2 == ${APP_EXENAME}
                StrCpy $exists 1
                ${Break}
              ${EndIf}
       ${Next}
       ${If} $exists == 0
          StrLen $0 ${ALPHABET}
          ${For} $R1 0 $0
            StrCpy $1 ${ALPHABET} 1 $R1
            ${IndexOf} $R0 $MRUList $1
            ${If} $R0 == -1
              WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\$ext\OpenWithList" $1 ${APP_EXENAME}
              StrCpy $MRUList "$MRUList$1"
              WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\$ext\OpenWithList" "MRUList" $MRUList
              ${Break}
            ${EndIf}
          ${Next}
       ${EndIf}
     not_mru:

     !insertmacro IfKeyExists ${REG_CLASSES_HKEY} "Software\Classes" "SystemFileAssociations"
     Pop $R0
     ${If} $R0 == 1
       !insertmacro IfKeyExists ${REG_CLASSES_HKEY} "Software\Classes\SystemFileAssociations\.$ext\Shell" ${VERB}
       Pop $R0
       ${If} $R0 == 0
         WriteRegStr ${REG_CLASSES_HKEY} "Software\Classes\SystemFileAssociations\.$ext\Shell\${VERB}" "" "${VERBNAME}"
         WriteRegStr ${REG_CLASSES_HKEY} "Software\Classes\SystemFileAssociations\.$ext\Shell\${VERB}\Command" "" '"$INSTDIR\${APP_EXENAME}" "%1"'
       ${EndIf}
     ${EndIf}
FunctionEnd


Section "FFDec" SecDummy
                                      
  SetShellVarContext all

  SetOutPath "$INSTDIR"
  
  File "dist\${APP_EXENAME}"
  File "dist\ffdec.bat"
  File "dist\ffdec.jar"
  File "dist\icon.ico"
  File "dist\license.txt"
  
  SetOutPath "$INSTDIR"  
  File /r "dist\lib"

 ;create start-menu items
!insertmacro MUI_STARTMENU_WRITE_BEGIN 0 ;This macro sets $SMDir and skips to MUI_STARTMENU_WRITE_END if the "Don't create shortcuts" checkbox is checked... 

  CreateDirectory "$SMPROGRAMS\$SMDir"
  CreateShortCut "$SMPROGRAMS\$SMDir\Uninstall ${APP_NAME}.lnk" "$INSTDIR\Uninstall.exe" "" "$INSTDIR\Uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\$SMDir\${APP_NAME}.lnk" "$INSTDIR\${APP_EXENAME}" "" "$INSTDIR\${APP_EXENAME}" 0
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

  call DownloadAndInstallJREIfNecessary
  call DownloadAndInstallFlashIfNecessary

SectionEnd




var txt
var pos
var pgfound
var f
var pgname
var pghtml

Section "$(STRING_SWC)" SecPlayerGlobal
;checkadobe:
DetailPrint "$(STRING_SWC_CHECK)"
GetTempFileName $pghtml
inetc::get /SILENT /USERAGENT "${APP_NAME} Setup" "https://www.adobe.com/support/flashplayer/downloads.html" "$pghtml" /END
Pop $0
StrCmp $0 "OK" dlok
MessageBox MB_OK "$(STRING_SWC_NOTFOUND)"
Goto exit
dlok:
StrCpy $pgfound 0

FileOpen $f "$pghtml" r
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
  MessageBox MB_OK "$(STRING_SWC_NOTFOUND)"
  Goto exit

  ${StrRPos} $pos $txt "/"
  IntOp $pos $pos + 1
  StrCpy $pgname $txt "" $pos    
  SetShellVarContext current    

  IfFileExists "$APPDATA\JPEXS\FFDec\flashlib\$pgname" swcexists
    CreateDirectory "$APPDATA\JPEXS\FFDec\flashlib"
    DetailPrint "$(STRING_STARTING_DOWNLOAD) PlayerGlobal.swc"
    inetc::get /USERAGENT "${APP_NAME} Setup" $txt "$APPDATA\JPEXS\FFDec\flashlib\$pgname" /END
    Pop $0
    StrCmp $0 "OK" saved
    MessageBox MB_OK "$(STRING_SWC_NOTFOUND)"
    Goto exit

    saved:
     DetailPrint "PlayerGlobal.swc $(STRING_SAVED_TO) $APPDATA\JPEXS\FFDec\flashlib\$pgname"
  Goto exit
  swcexists:
     DetailPrint "$APPDATA\JPEXS\FFDec\flashlib\$pgname $(STRING_EXISTS_SKIP_DOWNLOAD)"
  exit:
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

Section "$(STRING_ADD_CONTEXT_MENU)" SecContextMenu
    SetRegView 64
    Push "swf"
    Call AddToExtContextMenu
    Push "gfx"
    Call AddToExtContextMenu
    
    SetRegView 32
    Push "swf"
    Call AddToExtContextMenu
    Push "gfx"
    Call AddToExtContextMenu

SectionEnd

;--------------------------------
;Descriptions

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecDummy} "$(STRING_SECTION_APP)"
    !insertmacro MUI_DESCRIPTION_TEXT ${SecPlayerGlobal} "$(STRING_SECTION_SWC)" 
    !insertmacro MUI_DESCRIPTION_TEXT ${SecContextMenu} "$(STRING_SECTION_CONTEXT_MENU)"
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
  


  SetRegView 64
  Push "swf"
  Call un.RemoveExtContextMenu
  Push "gfx"
  Call un.RemoveExtContextMenu
  
  SetRegView 32
  Push "swf"
  Call un.RemoveExtContextMenu
  Push "gfx"
  Call un.RemoveExtContextMenu

 
  StrCmp $uninstlocal 1 0 +5
    SetShellVarContext current      
    RmDir /r "$APPDATA\JPEXS\FFDec\*.*"
    RmDir "$APPDATA\JPEXS\FFDec"
    RmDir "$APPDATA\JPEXS"

  DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${APP_UNINSTKEY}"

SectionEnd
