ConstantPool "_global", "com", "Object", "jpexs", "flash", "test", "testcases", "TestSuper2SetterGetter", "TestSuperSetterGetter", "prototype", "testSuperGetSet", "__set__myvar", "__get__myvar", "ASSetPropFlags"
Push "_global"
GetVariable
Push "com"
GetMember
Not
Not
If loc00ce
Push "_global"
GetVariable
Push "com", 0.0, "Object"
NewObject
SetMember
loc00ce:Pop
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Not
Not
If loc0106
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs", 0.0, "Object"
NewObject
SetMember
loc0106:Pop
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
If loc014a
Push "_global"
GetVariable
Push "com"
GetMember
Push "jpexs"
GetMember
Push "flash", 0.0, "Object"
NewObject
SetMember
loc014a:Pop
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
If loc019a
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
loc019a:Pop
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
If loc01f6
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
loc01f6:Pop
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
Push "TestSuper2SetterGetter"
GetMember
Not
Not
If loc03fa
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
Push "TestSuper2SetterGetter"
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
Push "TestSuper2SetterGetter"
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
Push "TestSuperSetterGetter"
GetMember
Extends
Push register1, "prototype"
GetMember
StoreRegister 2
Pop
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
Push "TestSuper2SetterGetter"
GetMember
Push "prototype"
GetMember
Push 3, "ASSetPropFlags"
CallFunction
loc03fa:Pop
