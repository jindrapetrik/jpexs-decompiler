ConstantPool "test", "globalFunctionsTest", "k", "Array", "a", "b", "Boolean", "c", "A", "clearInterval", "clearTimeout", "mc", "copy", "how", "escape", "f", "http://localhost/", "wnd", "/:$version", "loaded", "isFinite", "isNaN", "http://localhost/test.swf", "_level5", "http://localhost/vars.txt", "_level4", "aaaa", "destroyPC", "MMExecute", "Object", "parseFloat", "parseInt", "print:#bframe", "printasbitmap:#bframe", "tst", "setInterval", "ts", "setTimeout", "showRedrawRegions", "aa", "told", "unescape", "", "updateAfterEvent"
DefineFunction "tst", 1, "p1"  {
Push "test"
Trace
}
Push "globalFunctionsTest"
Trace
Push "k", 3, 2, 1, 3, "Array"
CallFunction
DefineLocal
Push "a", 1
DefineLocal
Push "b", "a"
GetVariable
Push 1, "Boolean"
CallFunction
DefineLocal
Push 5
Call
Push "c", "A"
DefineLocal
Push 5, 1, "clearInterval"
CallFunction
Pop
Push 4, 1, "clearTimeout"
CallFunction
Pop
Push "mc"
DefineLocal2
Push "mc"
GetVariable
Push "copy", 16389
CloneSprite
Push "a", "how", 1, "escape"
CallFunction
SetVariable
Push "f", "a"
GetVariable
DefineLocal
GetURL "FSCommand:alert(\"hi\");", ""
Push "a", "mc"
GetVariable
Push 6
GetProperty
SetVariable
Push "a"
GetTime
SetVariable
Push "http://localhost/", "wnd"
GetURL2 false, false, 2
Push "a", "/:$version"
GetVariable
SetVariable
GotoFrame 4
Play
GotoFrame 7
WaitForFrame 4, 2
Push "loaded"
Trace
Push "a", "f"
GetVariable
ToInteger
SetVariable
Push "a", "f"
GetVariable
Push 1, "isFinite"
CallFunction
SetVariable
Push "a", "f"
GetVariable
Push 1, "isNaN"
CallFunction
SetVariable
Push "a", "f"
GetVariable
StringLength
SetVariable
Push "http://localhost/test.swf", "a"
GetVariable
GetURL2 false, true, 1
Push "http://localhost/test.swf", "_level5"
GetURL2 false, false, 1
Push "http://localhost/vars.txt", "a"
GetVariable
GetURL2 true, true, 1
Push "http://localhost/vars.txt", "_level4"
GetURL2 true, false, 1
Push "a", "f"
GetVariable
MBAsciiToChar
SetVariable
Push "a", "f"
GetVariable
MBStringLength
SetVariable
Push "a", "f"
GetVariable
MBCharToAscii
SetVariable
Push "a", "aaaa", 5, 4
MBStringExtract
SetVariable
Push "destroyPC", 1, "MMExecute"
CallFunction
Pop
NextFrame
GotoFrame 0
Push "a", "f"
GetVariable
ToNumber
SetVariable
Push "a", "f"
GetVariable
Push 1, "Object"
CallFunction
SetVariable
Push "a", "f"
GetVariable
CharToAscii
SetVariable
Push "a", "f"
GetVariable
Push 1, "parseFloat"
CallFunction
SetVariable
Push "a", 16, "f"
GetVariable
Push 2, "parseInt"
CallFunction
SetVariable
Play
PrevFrame
GotoFrame 0
Push "print:#bframe", "mc"
GetVariable
GetURL2 false, false, 0
Push "printasbitmap:#bframe", "mc"
GetVariable
GetURL2 false, false, 0
Push "printasbitmap:#bframe", "_level5"
GetURL2 false, false, 0
Push "print:#bframe", "_level4"
GetURL2 false, false, 0
Push "a", 10
RandomNumber
SetVariable
Push "mc"
GetVariable
RemoveSprite
Push "f"
GetVariable
Push 5, "tst"
GetVariable
Push 3, "setInterval"
CallFunction
Pop
Push "mc"
GetVariable
Push 6f, 25
SetProperty
Push "f"
GetVariable
Push 5, "ts"
GetVariable
Push 3, "setTimeout"
CallFunction
Pop
Push 0.0, false, 2, "showRedrawRegions"
CallFunction
Pop
Push 5, 5, 6, 6, 1, 1, "mc"
GetVariable
StartDrag
Stop
StopSounds
EndDrag
Push "a", "f"
GetVariable
ToString
SetVariable
Push "a", "aa"
SetVariable
Push "f"
GetVariable
TargetPath
Pop
Push "mc"
GetVariable
SetTarget2
Push "told"
Trace
SetTarget ""
ToggleQuality
Push "a", "f"
GetVariable
Push 1, "unescape"
CallFunction
SetVariable
Push "", "mc"
GetVariable
GetURL2 false, true, 0
GetURL "", "_level4"
Push 0.0, "updateAfterEvent"
CallFunction
Pop
