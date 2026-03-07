DefineFunction2 "f", 1, 3, false, false, true, false, true, false, true, false, false, 2, "tst" {
Push register2
Not
If loc002e
Push 1
StoreRegister 1
Pop
Jump loc003b
loc002e:Push 2
StoreRegister 1
Pop
}
loc003b:Push "testVarDefineInFunc"
Trace
Push "tst"
GetVariable
Push 1, "f"
CallFunction
Pop
