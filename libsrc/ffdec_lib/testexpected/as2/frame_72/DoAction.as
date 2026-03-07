DefineFunction2 "f", 0, 5, false, false, true, false, true, false, true, false, false {
Push 0.0
InitObject
StoreRegister 3
Pop
Push 0.0
InitObject
StoreRegister 2
Pop
Push register3
Enumerate2
loc0036:StoreRegister 0
Push null
Equals2
If loc00b5
Push register0
StoreRegister 4
Pop
Push register2, register4
GetMember
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push "A"
StrictEquals
If loc0091
Push register0, "B"
StrictEquals
If loc0091
Push register0, "C"
StrictEquals
If loc0091
Jump loc00b0
loc0091:Push "Ret 5"
Trace
loc009c:Push null
Equals2
Not
If loc009c
Push 5
Return
loc00b0:Jump loc0036
loc00b5:Push "Final"
Trace
Push 10
Return
}
Push "forInSwitchTest"
Trace
