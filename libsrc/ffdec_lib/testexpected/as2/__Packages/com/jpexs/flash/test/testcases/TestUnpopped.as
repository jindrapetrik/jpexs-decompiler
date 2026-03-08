ConstantPool "_global", "com", "Object", "jpexs", "flash", "test", "testcases", "TestUnpopped", "prototype", "run", "a", "b", "c", "ASSetPropFlags"
Push "_global"
GetVariable
Push "com"
GetMember
Not
Not
If loc008e
Push "_global"
GetVariable
Push "com", 0.0, "Object"
NewObject
SetMember
loc008e:Pop
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Not
Not
If loc00c6
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs", 0.0, "Object"
NewObject
SetMember
loc00c6:Pop
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
If loc010a
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Push "flash", 0.0, "Object"
NewObject
SetMember
loc010a:Pop
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
If loc015a
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
loc015a:Pop
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
If loc01b6
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
loc01b6:Pop
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
Push "TestUnpopped"
GetMember
Not
Not
If loc029f
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
Push "TestUnpopped"
DefineFunction "", 0 {
}
StoreRegister 1
SetMember
Push register1, "prototype"
GetMember
StoreRegister 2
Pop
Push register2, "run"
DefineFunction2 "", 0, 2, false, false, true, false, true, false, false, true, false {
Push register1, "a"
GetMember
Push "b"
Trace
Push register1, "c"
GetMember
Not
If loc0259
Push "c"
Trace
}
loc0259:SetMember
Push register2, "c", true
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
Push "TestUnpopped"
GetMember
Push "prototype"
GetMember
Push 3, "ASSetPropFlags"
CallFunction
loc029f:Pop
