ConstantPool "a", "functionInnerParametersTest"
DefineFunction2 "outfunc", 0, 5, false, false, true, false, true, false, true, false, false {
DefineFunction2 "", 2, 5, false, false, true, false, true, false, true, false, false, 0, "a", 4, "x" {
Push "a"
GetVariable
Push 3
Add2
Push register4
Add2
StoreRegister 3
Pop
DefineFunction2 "", 0, 2, false, false, true, false, true, false, true, false, false {
Push "a"
GetVariable
Push 2
Add2
StoreRegister 1
Pop
Push register1
Trace
Push register1
Return
}
StoreRegister 2
Pop
Push 0.0, register2, undefined
CallMethod
Return
}
StoreRegister 2
Pop
Push 2, 5, 2, register2, undefined
CallMethod
Return
}
Push "functionInnerParametersTest"
Trace
