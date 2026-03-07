ConstantPool "a0", "a1", "r", "Math", "random", "floor", "a", "b", "c", "registersVsDefineLocalTest", "x1", "x2", "x3"
DefineFunction2 "x1", 1, 4, false, false, true, false, true, false, true, false, false, 3, "c" {
Push 1
StoreRegister 2
Pop
Push 2
StoreRegister 1
Pop
Push register2, register1, register3
Multiply
Add2
Return
}
DefineFunction "x2", 1, "c"  {
Push "a0", 1
DefineLocal
Push "a1", 2
DefineLocal
Push "r", 0.0, "Math"
GetVariable
Push "random"
CallMethod
Push 2
Multiply
Push 1, "Math"
GetVariable
Push "floor"
CallMethod
DefineLocal
Push "a", "r"
GetVariable
Add2
GetVariable
Push "b"
GetVariable
Push "c"
GetVariable
Multiply
Add2
Return
}
DefineFunction2 "x3", 1, 5, false, false, true, false, true, false, true, false, false, 4, "c" {
Push 1
StoreRegister 2
Pop
Push 2
StoreRegister 3
Pop
Push 0.0, "Math"
GetVariable
Push "random"
CallMethod
Push 2
Multiply
Push 1, "Math"
GetVariable
Push "floor"
CallMethod
StoreRegister 1
Pop
Push "a", register1
Add2
Push 12
SetVariable
Push register2, "b"
GetVariable
Push register4
Multiply
Add2
Return
}
Push "registersVsDefineLocalTest"
Trace
Push 2, 1, "x1"
CallFunction
Push 3, 1, "x2"
CallFunction
Add2
Push 4, 1, "x3"
CallFunction
Add2
Trace
