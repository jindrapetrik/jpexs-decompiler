ConstantPool "_global", "com", "Object", "jpexs", "flash", "test", "testcases", "TestSetterGetter", "prototype", "__get__mystvar", "_mystvar", "__set__mystvar", "__get__myvar", "_myvar", "__set__myvar", "__get__myvargetonly", "_myvargetonly", "__set__myvarsetonly", "_myvarsetonly", "__get__myvarsetonly", "classic", "okay", "mystvar", "addProperty", "myvar", "myvargetonly", "myvarsetonly", "ASSetPropFlags"
Push "_global"
GetVariable
Push "com"
GetMember
Not
Not
If loc0169
Push "_global"
GetVariable
Push "com", 0.0, "Object"
NewObject
SetMember
loc0169:Pop
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Not
Not
If loc01a1
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs", 0.0, "Object"
NewObject
SetMember
loc01a1:Pop
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
If loc01e5
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Push "flash", 0.0, "Object"
NewObject
SetMember
loc01e5:Pop
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
If loc0235
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
loc0235:Pop
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
If loc0291
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
loc0291:Pop
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
Push "TestSetterGetter"
GetMember
Not
Not
If loc0554
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
Push "TestSetterGetter"
DefineFunction "", 0 {
}
StoreRegister 1
SetMember
Push register1, "prototype"
GetMember
StoreRegister 2
Pop
Push register1, "__get__mystvar"
DefineFunction "", 0 {
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
Push "TestSetterGetter"
GetMember
Push "_mystvar"
GetMember
Return
}
SetMember
Push register1, "__set__mystvar"
DefineFunction2 "", 1, 2, false, false, true, false, true, false, true, false, false, 1, "val" {
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
Push "TestSetterGetter"
GetMember
Push "_mystvar", register1
SetMember
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
Push register2, "__get__myvar"
DefineFunction2 "", 0, 2, false, false, true, false, true, false, false, true, false {
Push register1, "_myvar"
GetMember
Return
}
SetMember
Push register2, "__set__myvar"
DefineFunction2 "", 1, 3, false, false, true, false, true, false, false, true, false, 2, "val" {
Push register1, "_myvar", register2
SetMember
Push 0.0, register1, "__get__myvar"
CallMethod
Return
}
SetMember
Push register2, "__get__myvargetonly"
DefineFunction2 "", 0, 2, false, false, true, false, true, false, false, true, false {
Push register1, "_myvargetonly"
GetMember
Return
}
SetMember
Push register2, "__set__myvarsetonly"
DefineFunction2 "", 1, 3, false, false, true, false, true, false, false, true, false, 2, "val" {
Push register1, "_myvarsetonly", register2
SetMember
Push 0.0, register1, "__get__myvarsetonly"
CallMethod
Return
}
SetMember
Push register2, "classic"
DefineFunction "", 0 {
Push "okay"
Trace
}
SetMember
Push register2, "_myvar", 1
SetMember
Push register1, "_mystvar", 2
SetMember
Push register2, "_myvarsetonly", 3
SetMember
Push register2, "_myvargetonly", 4
SetMember
Push register1, "__set__mystvar"
GetMember
Push register1, "__get__mystvar"
GetMember
Push "mystvar", 3, register1, "addProperty"
CallMethod
Push register2, "__set__myvar"
GetMember
Push register2, "__get__myvar"
GetMember
Push "myvar", 3, register2, "addProperty"
CallMethod
DefineFunction "", 0 {
}
Push register2, "__get__myvargetonly"
GetMember
Push "myvargetonly", 3, register2, "addProperty"
CallMethod
Push register2, "__set__myvarsetonly"
GetMember
DefineFunction "", 0 {
}
Push "myvarsetonly", 3, register2, "addProperty"
CallMethod
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
Push "TestSetterGetter"
GetMember
Push "prototype"
GetMember
Push 3, "ASSetPropFlags"
CallFunction
loc0554:Pop
