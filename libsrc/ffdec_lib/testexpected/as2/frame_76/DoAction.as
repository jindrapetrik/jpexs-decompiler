ConstantPool "compoundAssignmentsTest", "a", "x", "b", "c", "f"
DefineFunction2 "f", 0, 4, false, false, true, false, false, true, true, false, false {
Push register1, 0.0
GetMember
Trace
Push 0.0
StoreRegister 2
Pop
Push register2, 20
Add2
StoreRegister 2
Pop
Push register2, 20
Add2
StoreRegister 2
StoreRegister 3
Pop
}
Push "compoundAssignmentsTest"
Trace
Push "a", 0.0
DefineLocal
Push "a", "a"
GetVariable
Push 5
Add2
SetVariable
Push "x", "a", "a"
GetVariable
Push 5
Add2
StoreRegister 0
SetVariable
Push register0
DefineLocal
Push "a"
GetVariable
Push "b"
GetMember
Push "c", "a"
GetVariable
Push "b"
GetMember
Push "c"
GetMember
Push 10
Add2
SetMember
Push "x", "a"
GetVariable
Push "b"
GetMember
Push "c", "a"
GetVariable
Push "b"
GetMember
Push "c"
GetMember
Push 10
Add2
StoreRegister 0
SetMember
Push register0
SetVariable
Push 5, 1, "f"
CallFunction
Pop
Push "a"
GetVariable
Push "b"
GetMember
Push "c", "a"
GetVariable
Push "b"
GetMember
Push "c"
GetMember
Push 30
Add2
StoreRegister 0
SetMember
Push register0
Trace
