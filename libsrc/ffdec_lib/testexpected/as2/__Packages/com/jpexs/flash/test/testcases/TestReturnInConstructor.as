ConstantPool "_global", "com", "Object", "jpexs", "flash", "test", "testcases", "TestReturnInConstructor", "A", "B", "prototype", "func", "ASSetPropFlags"
Push "_global"
GetVariable
Push "com"
GetMember
Not
Not
If loc0098
Push "_global"
GetVariable
Push "com", 0.0, "Object"
NewObject
SetMember
loc0098:Pop
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Not
Not
If loc00d0
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs", 0.0, "Object"
NewObject
SetMember
loc00d0:Pop
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
If loc0114
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Push "flash", 0.0, "Object"
NewObject
SetMember
loc0114:Pop
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
If loc0164
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
loc0164:Pop
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
If loc01c0
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
loc01c0:Pop
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
Push "TestReturnInConstructor"
GetMember
Not
Not
If loc02e7
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
Push "TestReturnInConstructor"
DefineFunction2 "", 0, 2, false, false, true, false, true, false, true, false, false {
Push 3
StoreRegister 1
Pop
Push register1, 3
Equals2
Not
If loc0249
Push "A"
Trace
Push undefined
Return
loc0249:Push "B"
Trace
}
StoreRegister 1
SetMember
Push register1, "prototype"
GetMember
StoreRegister 2
Pop
Push register2, "func"
DefineFunction2 "", 0, 2, false, false, true, false, true, false, true, false, false {
Push 3
StoreRegister 1
Pop
Push register1, 3
Equals2
Not
If loc029c
Push "A"
Trace
Push undefined
Return
loc029c:Push "B"
Trace
Push 5
Return
}
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
Push "TestReturnInConstructor"
GetMember
Push "prototype"
GetMember
Push 3, "ASSetPropFlags"
CallFunction
loc02e7:Pop
