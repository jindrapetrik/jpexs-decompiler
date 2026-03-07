DefineFunction2 "a", 0, 2, false, false, true, false, true, false, true, false, false {
Push "hi"
Trace
Push 5
StoreRegister 1
Pop
Push register1, 7
Equals2
Not
If loc0037
Push undefined
Return
loc0037:Push register1, 9
Multiply
StoreRegister 1
Pop
Push register1
Trace
}
Push "function2Test"
Trace
