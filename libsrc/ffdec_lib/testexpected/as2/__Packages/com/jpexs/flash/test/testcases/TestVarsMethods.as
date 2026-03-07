ConstantPool "_global", "com", "Object", "jpexs", "flash", "test", "testcases", "TestVarsMethods", "constructor", "prototype", "instMethod", "instance method", "statMethod", "static method", "instVar", "statVar", "ASSetPropFlags"
Push "_global"
GetVariable
Push "com"
GetMember
Not
Not
If loc00d7
Push "_global"
GetVariable
Push "com", 0.0, "Object"
NewObject
SetMember
loc00d7:Pop
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Not
Not
If loc010f
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs", 0.0, "Object"
NewObject
SetMember
loc010f:Pop
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
If loc0153
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Push "flash", 0.0, "Object"
NewObject
SetMember
loc0153:Pop
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
If loc01a3
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
loc01a3:Pop
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
If loc01ff
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
loc01ff:Pop
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
Push "TestVarsMethods"
GetMember
Not
Not
If loc02f5
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
Push "TestVarsMethods"
DefineFunction "", 0 {
Push "constructor"
Trace
}
StoreRegister 1
SetMember
Push register1, "prototype"
GetMember
StoreRegister 2
Pop
Push register2, "instMethod"
DefineFunction "", 0 {
Push "instance method"
Trace
}
SetMember
Push register1, "statMethod"
DefineFunction "", 0 {
Push "static method"
Trace
}
SetMember
Push register2, "instVar", 1
SetMember
Push register1, "statVar", 2
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
Push "TestVarsMethods"
GetMember
Push "prototype"
GetMember
Push 3, "ASSetPropFlags"
CallFunction
loc02f5:Pop
