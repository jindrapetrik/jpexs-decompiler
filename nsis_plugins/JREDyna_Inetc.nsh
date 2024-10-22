; Version 1.0.1

;JPEXS : Added 64 bit support
;JPEXS : Added getting latest JRE from java web


  !ifndef JRE_DECLARES
  !define JRE_DECLARES

  !include "WordFunc.nsh"
  !include "x64.nsh"


  !macro CUSTOM_PAGE_JREINFO
    Page custom CUSTOM_PAGE_JREINFO
  !macroend

  !ifndef JRE_VERSION
    !error "JRE_VERSION must be defined"
  !endif
 
  !ifndef JRE_DOWNLOAD_PAGE_URL
    !define JRE_DOWNLOAD_PAGE_URL "https://java.com/en/download/manual.jsp" 
  !endif

  !ifndef JRE_DOWNLOAD_ANCHOR_32  
    !define JRE_DOWNLOAD_ANCHOR_32 "Windows Offline" 
  !endif

  !ifndef JRE_DOWNLOAD_ANCHOR_64
    !define JRE_DOWNLOAD_ANCHOR_64 "Windows Offline (64-bit)" 
  !endif


;;;;;;;;;;;;;;;;;;;;;
;  Custom panel
;;;;;;;;;;;;;;;;;;;;;

Function CUSTOM_PAGE_JREINFO

  push $0
  push $1
  push $2
  
  
  Push "${JRE_VERSION}"
  Call DetectJRE
  Pop $0
  Pop $1
  StrCmp $0 "OK" exit

  nsDialogs::create /NOUNLOAD 1018
  pop $1

  StrCmp $0 "0" NoFound
  StrCmp $0 "-1" FoundOld


NoFound:
  !insertmacro MUI_HEADER_TEXT "$(STRING_JRE_REQUIRED_TITLE)" "$(STRING_JRE_REQUIRED)"
  ${NSD_CreateLabel} 0 0 100% 100% "$(STRING_JRE_REQUIRED_INFO)"
  pop $1
  goto ShowDialog

FoundOld:
  !insertmacro MUI_HEADER_TEXT "$(STRING_JRE_UPDATEREQUIRED_TITLE)" "$(STRING_JRE_UPDATEREQUIRED)"
  ${NSD_CreateLabel} 0 0 100% 100% "$(STRING_JRE_UPDATEREQUIRED_INFO)"
  pop $1
  goto ShowDialog

ShowDialog:

  nsDialogs::Show

exit:


  pop $2
  pop $1
  pop $0

FunctionEnd


Function Trim
    Exch $R1 ; Original string
    Push $R2
Loop:
    StrCpy $R2 "$R1" 1
    StrCmp "$R2" " " TrimLeft
    StrCmp "$R2" "$\r" TrimLeft
    StrCmp "$R2" "$\n" TrimLeft
    StrCmp "$R2" "$\t" TrimLeft
    GoTo Loop2
TrimLeft:
    StrCpy $R1 "$R1" "" 1
    Goto Loop
Loop2:
    StrCpy $R2 "$R1" 1 -1
    StrCmp "$R2" " " TrimRight
    StrCmp "$R2" "$\r" TrimRight
    StrCmp "$R2" "$\n" TrimRight
    StrCmp "$R2" "$\t" TrimRight
    GoTo Done
TrimRight:
    StrCpy $R1 "$R1" -1
    Goto Loop2
Done:
    Pop $R2
    Exch $R1
FunctionEnd
!define Trim "!insertmacro Trim"
 !macro Trim ResultVar String
  Push "${String}"
  Call Trim
  Pop "${ResultVar}"
!macroend



; Checks to ensure that the installed version of the JRE (if any) is at least that of
; the JRE_VERSION variable.  The JRE will be downloaded and installed if necessary
; The full path of java.exe will be returned on the stack


var JRE_URL
var JRE_ANCHOR
var jpghtml
var aurl
var astate
var atext
var jf
var jtxt
var jpos

Function DownloadAndInstallJREIfNecessary
  Push $0
  Push $1

  DetailPrint "$(STRING_JRE_DETECTVERSION)"
  Push "${JRE_VERSION}"
  Call DetectJRE
  Pop $0	; Get return value from stack
  Pop $1	; get JRE path (or error message)
  DetailPrint "$(STRING_JRE_DETECTCOMPLETE)$1"


  strcmp $0 "OK" End downloadJRE

downloadJRE:

  ${If} ${RunningX64}
    Push "${JRE_DOWNLOAD_ANCHOR_64}"
  ${Else}
    Push "${JRE_DOWNLOAD_ANCHOR_32}"
  ${EndIf}
  Pop $JRE_ANCHOR
  
  GetTempFileName $jpghtml
  inetc::get /SILENT /USERAGENT "${APP_NAME} Setup" "${JRE_DOWNLOAD_PAGE_URL}" "$jpghtml" /END
Pop $0
StrCmp $0 "OK" 0 urlcheckfailed

StrCpy $astate 0

FileOpen $jf "$jpghtml" r
jloop:
  FileRead $jf $jtxt
  IfErrors jdone      
      ${Trim} $jtxt $jtxt
      ;MessageBox MB_OK $jtxt 
      StrCmp $astate 0 astate0
      StrCmp $astate 1 astate1
      StrCmp $astate 2 astate2
      StrCmp $astate 3 astate3
      
      astate0:
      ${StrLoc} $jpos $jtxt "<a " ">"
      StrCmp $jpos "" jloop
      IntOp $jpos $jpos + 3 ;<a  len
      StrCpy $jtxt $jtxt "" $jpos      

      astate1:
      StrCpy $astate 1      
      ${StrLoc} $jpos $jtxt "href=$\"" ">"
      StrCmp $jpos "" jloop
      IntOp $jpos $jpos + 6 ;href=" len
      StrCpy $jtxt $jtxt "" $jpos
      ${StrLoc} $jpos $jtxt "$\"" ">"
      StrCmp $jpos "" jloop 
      StrCpy $aurl $jtxt $jpos
      IntOp $jpos $jpos + 1 ;" len      
      StrCpy $jtxt $jtxt "" $jpos        
    
      astate2:
      StrCpy $astate 2
      ${StrLoc} $jpos $jtxt ">" ">"
      StrCmp $jpos "" jloop 
      IntOp $jpos $jpos + 1 ;> len            
      StrCpy $jtxt $jtxt "" $jpos      
      
      StrCpy $atext ""

      astate3:
      StrCpy $astate 3      
      ${StrLoc} $jpos $jtxt "</a>" ">"
      StrCmp $jpos "" 0 telse      
        StrCpy $atext "$atext$jtxt"
        Goto jloop
      telse:
        StrCpy $jtxt $jtxt $jpos   
        StrCpy $atext "$atext$jtxt"        
        StrCpy $astate 0

        StrCmp $atext $JRE_ANCHOR 0 jloop
        StrCpy $JRE_URL $aurl               
jdone:
  FileClose $jf

  StrCmp $JRE_URL "" urlcheckfailed urlgetsuccessfull
  urlcheckfailed:
  MessageBox MB_ABORTRETRYIGNORE|MB_ICONSTOP "$(STRING_JRE_CANNOTDOWNLOAD)" /SD IDIGNORE IDRETRY downloadJRE IDIGNORE End
  Abort
  urlgetsuccessfull:
  
  DetailPrint "$(STRING_JRE_WILLDOWNLOAD)$JRE_URL"
  Inetc::get "$JRE_URL" "$TEMP\jre_Setup.exe" /END
  Pop $0 # return value = exit code, "OK" if OK
  DetailPrint "$(STRING_JRE_DOWNRESULT)$0"

  strcmp $0 "OK" downloadsuccessful
  MessageBox MB_ABORTRETRYIGNORE|MB_ICONSTOP "$(STRING_JRE_CANNOTDOWNLOAD)" /SD IDIGNORE IDRETRY downloadJRE IDIGNORE End
  Abort
 
downloadsuccessful:

  DetailPrint "$(STRING_JRE_LAUNCHSETUP)"
  
  IfSilent doSilent
  ExecWait '"$TEMP\jre_setup.exe" REBOOT=Suppress SPONSORS=0 /L "$TEMP\jre_setup.log"' $0
  goto jreSetupfinished
doSilent:
  ExecWait '"$TEMP\jre_setup.exe" /S REBOOT=Suppress SPONSORS=0 /L "$TEMP\jre_setup.log"' $0
  

jreSetupFinished:
  DetailPrint "$(STRING_JRE_SETUPFINISHED)"
  Delete "$TEMP\jre_setup.exe"
  StrCmp $0 "0" InstallVerify 0
  Push "$(STRING_JRE_INTERRUPTED)$0"
  Goto ExitInstallJRE
 
InstallVerify:
  DetailPrint "$(STRING_JRE_SETUPOUTCOME)"
  Push "${JRE_VERSION}"
  Call DetectJRE  
  Pop $0	  ; DetectJRE's return value
  Pop $1	  ; JRE home (or error message if compatible JRE could not be found)
  StrCmp $0 "OK" 0 JavaVerStillWrong
  Goto JREPathStorage
JavaVerStillWrong:
  Push "$(STRING_JRE_UNABLEFINDAFTER)$\n$\n$1"
  Goto ExitInstallJRE
 
JREPathStorage:
  push $0	; => rv, r1, r0
  exch 2	; => r0, r1, rv
  exch		; => r1, r0, rv
  Goto End
 
ExitInstallJRE:
  Pop $1
  MessageBox MB_ABORTRETRYIGNORE|MB_ICONSTOP "$(STRING_JRE_UNABLEINSTALL)$\n$\n$1" /SD IDIGNORE IDRETRY downloadJRE IDIGNORE End
  Pop $1 	; Restore $1
  Pop $0 	; Restore $0
  Abort
End:
  Pop $1	; Restore $1
  Pop $0	; Restore $0

FunctionEnd


; DetectJRE
; Inputs:  Minimum JRE version requested on stack (this value will be overwritten)
; Outputs: Returns two values on the stack: 
;     First value (rv0):  0 - JRE not found. -1 - JRE found but too old. OK - JRE found and meets version criteria
;     Second value (rv1):  Problem description.  Otherwise - Path to the java runtime (javaw.exe will be at .\bin\java.exe relative to this path)
 
Function DetectJRE

  Exch $0	; Get version requested  
		; Now the previous value of $0 is on the stack, and the asked for version of JDK is in $0
  Push $1	; $1 = Java version string (ie 1.5.0)
  Push $2	; $2 = Javahome
  Push $3	; $3 = holds the version comparison result

		; stack is now:  r3, r2, r1, r0
;DetectJRE64:
  SetRegView 64
  ; first, check for an installed JRE
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  StrCmp $1 "" DetectJDK64
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"
  StrCmp $2 "" DetectJDK64
  Goto GetJRE
 
DetectJDK64:
  SetRegView 64
  ; next, check for an installed JDK
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
  StrCmp $1 "" DetectJRE
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$1" "JavaHome"
  StrCmp $2 "" DetectJRE
  Goto GetJRE

DetectJRE:
  SetRegView 32
; first, check for an installed JRE
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  StrCmp $1 "" DetectJDK
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"
  StrCmp $2 "" DetectJDK
  Goto GetJRE


DetectJDK:
  SetRegView 32
  ; next, check for an installed JDK
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
  StrCmp $1 "" NoFound
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$1" "JavaHome"
  StrCmp $2 "" NoFound
  Goto GetJRE


GetJRE:
  SetRegView 32
  ; ok, we found a JRE, let's compare it's version and make sure it is new enough
; $0 = version requested. $1 = version found. $2 = javaHome
  IfFileExists "$2\bin\java.exe" 0 NoFound

  ${VersionCompare} $0 $1 $3 ; $3 now contains the result of the comparison
  DetailPrint "$(STRING_JRE_DETECTCOMPARE_1)$0$(STRING_JRE_DETECTCOMPARE_2)$1$(STRING_JRE_DETECTCOMPARE_3)$3"
  intcmp $3 1 FoundOld
  goto FoundNew
 
NoFound:
  ; No JRE found
  strcpy $0 "0"
  strcpy $1 "$(STRING_JRE_DETECTCOMPLETE_NO)"
  Goto DetectJREEnd
 
FoundOld:
  ; An old JRE was found
  strcpy $0 "-1"
  strcpy $1 "$(STRING_JRE_DETECTCOMPLETE_OLD)"
  Goto DetectJREEnd  
FoundNew:
  ; A suitable JRE was found 
  strcpy $0 "OK"
  strcpy $1 $2
  Goto DetectJREEnd

DetectJREEnd:
        SetRegView 32
	; at this stage, $0 contains rv0, $1 contains rv1
	; now, straighten the stack out and recover original values for r0, r1, r2 and r3
	; there are two return values: rv0 = -1, 0, OK and rv1 = JRE path or problem description
	; stack looks like this: 
                ;    r3,r2,r1,r0
	Pop $3	; => r2,r1,r0
	Pop $2	; => r1,r0
	Push $0 ; => rv0, r1, r0
	Exch 2	; => r0, r1, rv0
	Push $1 ; => rv1, r0, r1, rv0
	Exch 2	; => r1, r0, rv1, rv0
	Pop $1	; => r0, rv1, rv0
	Pop $0	; => rv1, rv0	
	Exch	; => rv0, rv1


FunctionEnd
  !endif ; // JRE_DECLARES