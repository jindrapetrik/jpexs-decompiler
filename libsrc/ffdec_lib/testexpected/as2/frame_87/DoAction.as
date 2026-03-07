ConstantPool "a", "callRegisterTest", "f", "A", "g", "B", "obj", "b", "tst"
DefineFunction2 "tst", 1, 3, false, false, true, false, true, false, true, false, false, 2, "o" {
Push "a"
StoreRegister 1
Pop
Push 0.0, register2, register1
CallMethod
Pop
}
Push "callRegisterTest"
Trace
Push "f"
DefineFunction "", 0 {
Push "A"
Trace
}
DefineLocal
Push "g"
DefineFunction "", 0 {
Push "B"
Trace
}
DefineLocal
Push "obj", "a", "f"
GetVariable
Push "b", "g"
GetVariable
Push 2
InitObject
DefineLocal
Push "obj"
GetVariable
Push 1, "tst"
CallFunction
Pop
