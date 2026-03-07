ConstantPool "_global", "com", "Object", "jpexs", "flash", "test", "testcases", "TestSuperSetterGetter", "TestSetterGetter", "prototype", "__get__myvar2", "_myvar2", "__set__myvar2", "testThisGetSet", "testThisParentGetSet", "__set__myvar", "__get__myvar", "testSuperGetSet", "myvar2", "addProperty", "ASSetPropFlags"
Push "_global"
GetVariable
Push "com"
GetMember
Not
Not
If loc0123
Push "_global"
GetVariable
Push "com", 0.0, "Object"
NewObject
SetMember
loc0123:Pop
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Not
Not
If loc015b
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs", 0.0, "Object"
NewObject
SetMember
loc015b:Pop
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
If loc019f
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Push "flash", 0.0, "Object"
NewObject
SetMember
loc019f:Pop
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
If loc01ef
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
loc01ef:Pop
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
If loc024b
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
loc024b:Pop
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
Push "TestSuperSetterGetter"
GetMember
Not
Not
If loc069f
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
Push "TestSuperSetterGetter"
DefineFunction2 "", 0, 2, false, false, false, true, true, false, true, false, false {
Push 0.0, register1, undefined
CallMethod
Pop
}
StoreRegister 1
SetMember
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
Push "TestSuperSetterGetter"
GetMember
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
Extends
Push register1, "prototype"
GetMember
StoreRegister 2
Pop
Push register2, "__get__myvar2"
DefineFunction2 "", 0, 2, false, false, true, false, true, false, false, true, false {
Push register1, "_myvar2"
GetMember
Return
}
SetMember
Push register2, "__set__myvar2"
DefineFunction2 "", 1, 3, false, false, true, false, true, false, false, true, false, 2, "val" {
Push register1, "_myvar2", register2
SetMember
Push 0.0, register1, "__get__myvar2"
CallMethod
Return
}
SetMember
Push register2, "testThisGetSet"
DefineFunction2 "", 0, 2, false, false, true, false, true, false, false, true, false {
Push 2, 1, register1, "__set__myvar2"
CallMethod
Pop
Push 0.0, register1, "__get__myvar2"
CallMethod
Trace
Push 0.0, 0.0, register1, "__get__myvar2"
CallMethod
Push undefined
CallMethod
Pop
Push 0.0, 0.0, register1, "__get__myvar2"
CallMethod
Push undefined
NewMethod
Pop
Push 0.0, register1, "__get__myvar2"
CallMethod
Increment
Push 1, register1, "__set__myvar2"
CallMethod
Pop
Push 0.0, register1, "__get__myvar2"
CallMethod
Push 0.0, register1, "__get__myvar2"
CallMethod
Increment
Push 1, register1, "__set__myvar2"
CallMethod
Pop
Trace
Push 0.0, register1, "__get__myvar2"
CallMethod
Increment
Push 1, register1, "__set__myvar2"
CallMethod
Trace
}
SetMember
Push register2, "testThisParentGetSet"
DefineFunction2 "", 0, 2, false, false, true, false, true, false, false, true, false {
Push 2, 1, register1, "__set__myvar"
CallMethod
Pop
Push 0.0, register1, "__get__myvar"
CallMethod
Trace
Push 0.0, 0.0, register1, "__get__myvar"
CallMethod
Push undefined
CallMethod
Pop
Push 0.0, 0.0, register1, "__get__myvar"
CallMethod
Push undefined
NewMethod
Pop
Push 0.0, register1, "__get__myvar"
CallMethod
Increment
Push 1, register1, "__set__myvar"
CallMethod
Pop
Push 0.0, register1, "__get__myvar"
CallMethod
Push 0.0, register1, "__get__myvar"
CallMethod
Increment
Push 1, register1, "__set__myvar"
CallMethod
Pop
Trace
Push 0.0, register1, "__get__myvar"
CallMethod
Increment
Push 1, register1, "__set__myvar"
CallMethod
Trace
}
SetMember
Push register2, "testSuperGetSet"
DefineFunction2 "", 0, 2, false, false, false, true, true, false, true, false, false {
Push 3, 1, register1, "__set__myvar"
CallMethod
Pop
Push 0.0, register1, "__get__myvar"
CallMethod
Trace
Push 0.0, 0.0, register1, "__get__myvar"
CallMethod
Push undefined
CallMethod
Pop
Push 0.0, 0.0, register1, "__get__myvar"
CallMethod
Push undefined
NewMethod
Pop
Push 0.0, register1, "__get__myvar"
CallMethod
Delete2
Pop
Push 0.0, register1, "__get__myvar"
CallMethod
Increment
Push 1, register1, "__set__myvar"
CallMethod
Pop
Push 0.0, register1, "__get__myvar"
CallMethod
Push 0.0, register1, "__get__myvar"
CallMethod
Increment
Push 1, register1, "__set__myvar"
CallMethod
Pop
Trace
Push 0.0, register1, "__get__myvar"
CallMethod
Increment
Push 1, register1, "__set__myvar"
CallMethod
Trace
}
SetMember
Push register2, "_myvar2", 1
SetMember
Push register2, "__set__myvar2"
GetMember
Push register2, "__get__myvar2"
GetMember
Push "myvar2", 3, register2, "addProperty"
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
Push "TestSuperSetterGetter"
GetMember
Push "prototype"
GetMember
Push 3, "ASSetPropFlags"
CallFunction
loc069f:Pop
