ConstantPool "c", "hi", "e", "d", "f", "dd", "function3Test", "tst"
DefineFunction2 "tst", 0, 2, false, false, true, false, true, false, true, false, false {
Push 5
StoreRegister 1
Pop
Push "c", 8
StoreRegister 1
SetVariable
Push "hi"
Trace
Push register1
Trace
Push "e", "d", "f", "c", 9
StoreRegister 0
SetVariable
Push register0
StoreRegister 0
SetVariable
Push register0
StoreRegister 0
SetVariable
Push register0
StoreRegister 0
SetVariable
Push register0, 5
Greater
Not
If loc00a5
Push "dd"
Trace
}
loc00a5:Push "function3Test"
Trace
Push "c", 7
DefineLocal
Push "d", 7
DefineLocal
Push "e", 8
DefineLocal
Push 0.0, "tst"
CallFunction
Pop
