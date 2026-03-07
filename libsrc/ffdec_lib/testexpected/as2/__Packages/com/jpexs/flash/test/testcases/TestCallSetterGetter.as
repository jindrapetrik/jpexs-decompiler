ConstantPool "_global", "com", "Object", "jpexs", "flash", "test", "testcases", "TestCallSetterGetter", "prototype", "testSetterCall", "myobj", "__set__myvar", "testGetterCall", "__get__myvar", "testStatGetterCall", "TestSetterGetter", "__get__mystvar", "testStatSetterCall", "__set__mystvar", "ASSetPropFlags"
Push "_global"
GetVariable
Push "com"
GetMember
Not
Not
If loc011f
Push "_global"
GetVariable
Push "com", 0.0, "Object"
NewObject
SetMember
loc011f:Pop
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Not
Not
If loc0157
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs", 0.0, "Object"
NewObject
SetMember
loc0157:Pop
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
If loc019b
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Push "flash", 0.0, "Object"
NewObject
SetMember
loc019b:Pop
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
If loc01eb
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
loc01eb:Pop
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
If loc0247
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
loc0247:Pop
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
Push "TestCallSetterGetter"
GetMember
Not
Not
If loc03d5
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
Push "TestCallSetterGetter"
DefineFunction "", 0 {
}
StoreRegister 1
SetMember
Push register1, "prototype"
GetMember
StoreRegister 2
Pop
Push register2, "testSetterCall"
DefineFunction2 "", 0, 2, false, false, true, false, true, false, false, true, false {
Push 5, 1, register1, "myobj"
GetMember
Push "__set__myvar"
CallMethod
Pop
}
SetMember
Push register2, "testGetterCall"
DefineFunction2 "", 0, 2, false, false, true, false, true, false, false, true, false {
Push 0.0, register1, "myobj"
GetMember
Push "__get__myvar"
CallMethod
Return
}
SetMember
Push register2, "testStatGetterCall"
DefineFunction "", 0 {
Push 0.0, "com"
GetVariable
Push "jpexs"
GetMember
Push "flash"
GetMember
Push "test"
GetMember
Push "testcases"
GetMember
Push "TestSetterGetter"
GetMember
Push "__get__mystvar"
CallMethod
Return
}
SetMember
Push register2, "testStatSetterCall"
DefineFunction "", 1, "val"  {
Push 6, 1, "com"
GetVariable
Push "jpexs"
GetMember
Push "flash"
GetMember
Push "test"
GetMember
Push "testcases"
GetMember
Push "TestSetterGetter"
GetMember
Push "__set__mystvar"
CallMethod
Pop
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
Push "TestCallSetterGetter"
GetMember
Push "prototype"
GetMember
Push 3, "ASSetPropFlags"
CallFunction
loc03d5:Pop
