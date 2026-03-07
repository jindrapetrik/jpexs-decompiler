ConstantPool "_global", "com", "Object", "jpexs", "flash", "test", "testcases", "TestMaintainOrder", "prototype", "h", "8", "i", "9", "j", "10", "k", "11", "l", "12", "m", "13", "_x2", "after _x1", "a", "b", "c", "d", "e", "f", "g", "_x1", "after method m", "ASSetPropFlags"
Push "_global"
GetVariable
Push "com"
GetMember
Not
Not
If loc00d4
Push "_global"
GetVariable
Push "com", 0.0, "Object"
NewObject
SetMember
loc00d4:Pop
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Not
Not
If loc010c
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs", 0.0, "Object"
NewObject
SetMember
loc010c:Pop
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Push "flash"
GetMember
Not
Not
If loc0150
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Push "flash", 0.0, "Object"
NewObject
SetMember
loc0150:Pop
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Push "flash"
GetMember
Push "test"
GetMember
Not
Not
If loc01a0
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Push "flash"
GetMember
Push "test", 0.0, "Object"
NewObject
SetMember
loc01a0:Pop
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Push "flash"
GetMember
Push "test"
GetMember
Push "testcases"
GetMember
Not
Not
If loc01fc
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Push "flash"
GetMember
Push "test"
GetMember
Push "testcases", 0.0, "Object"
NewObject
SetMember
loc01fc:Pop
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Push "flash"
GetMember
Push "test"
GetMember
Push "testcases"
GetMember
Push "TestMaintainOrder"
GetMember
Not
Not
If loc03a5
Push "com"
GetVariable
Push "jpexs"
GetMember
Push "flash"
GetMember
Push "test"
GetMember
Push "testcases"
GetMember
Push "TestMaintainOrder"
DefineFunction "", 0 {
}
StoreRegister 1
SetMember
Push register1, "prototype"
GetMember
StoreRegister 2
Pop
Push register2, "h"
DefineFunction "", 0 {
Push "8"
Trace
}
SetMember
Push register2, "i"
DefineFunction "", 0 {
Push "9"
Trace
}
SetMember
Push register1, "j"
DefineFunction "", 0 {
Push "10"
Trace
}
SetMember
Push register1, "k"
DefineFunction "", 0 {
Push "11"
Trace
}
SetMember
Push register2, "l"
DefineFunction "", 0 {
Push "12"
Trace
}
SetMember
Push register1, "m"
DefineFunction "", 0 {
Push "13"
Trace
}
SetMember
Push register2, "_x2"
DefineFunction "", 0 {
Push "after _x1"
Trace
}
SetMember
Push register2, "a", 1
SetMember
Push register1, "b", 2
SetMember
Push register2, "c", 3
SetMember
Push register1, "d", 4
SetMember
Push register1, "e", 5
SetMember
Push register2, "f", 6
SetMember
Push register2, "g", 7
SetMember
Push register2, "_x1", "after method m"
SetMember
Push 1, null, "com"
GetVariable
Push "jpexs"
GetMember
Push "flash"
GetMember
Push "test"
GetMember
Push "testcases"
GetMember
Push "TestMaintainOrder"
GetMember
Push "prototype"
GetMember
Push 3, "ASSetPropFlags"
CallFunction
loc03a5:Pop
