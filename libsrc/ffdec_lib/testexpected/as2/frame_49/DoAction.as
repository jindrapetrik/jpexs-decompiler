DefineFunction2 "tst", 1, 2, false, false, true, false, true, false, true, false, false, 0, "px" {
Push 57
StoreRegister 1
Pop
Push register1, 27
Multiply
StoreRegister 1
Pop
}
Push "registersFuncTest"
Trace
Push 5, 1, "tst"
CallFunction
Pop
Push "s", 5
ToString
DefineLocal
