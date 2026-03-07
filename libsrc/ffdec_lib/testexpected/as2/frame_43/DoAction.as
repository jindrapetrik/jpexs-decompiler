ConstantPool "incDecTest", "i", "b", "c", "a:", "a", " b:", " c:", "arr", "d", "tst"
DefineFunction "tst", 0 {
Push 1
Return
}
Push "incDecTest"
Trace
Push "i", 5
DefineLocal
Push "b", "i"
GetVariable
Push "i", "i"
GetVariable
Increment
SetVariable
DefineLocal
Push "c", "i", "i"
GetVariable
Decrement
StoreRegister 0
SetVariable
Push register0, 5
Add2
DefineLocal
Push "a:", "a"
GetVariable
Add2
Push " b:"
Add2
Push "b"
GetVariable
Add2
Push " c:"
Add2
Push "c"
GetVariable
Add2
Trace
Push "arr", 3, 2, 1, 3
InitArray
DefineLocal
Push "d", "arr"
GetVariable
Push 0.0, "tst"
CallFunction
GetMember
Push "arr"
GetVariable
Push 0.0, "tst"
CallFunction
Push "arr"
GetVariable
Push 0.0, "tst"
CallFunction
GetMember
Increment
SetMember
DefineLocal
Push "d"
GetVariable
Trace
