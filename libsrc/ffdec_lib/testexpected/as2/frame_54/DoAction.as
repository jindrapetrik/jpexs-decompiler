DefineFunction2 "tst", 0, 2, false, false, true, false, true, false, true, false, false {
Push 5
StoreRegister 1
Pop
loc001b:Push register1, 10
Less2
Not
If loc006f
Push register1, 5
Equals2
Not
If loc0059
Push register1, 6
Equals2
Not
If loc0054
Push true
Return
loc0054:Jump loc005f
loc0059:Push false
Return
loc005f:Push register1
Increment
StoreRegister 1
Pop
Jump loc001b
}
loc006f:Push "function4Test"
Trace
Push 0.0, "tst"
CallFunction
Pop
