DefineFunction2 "tst", 0, 4, false, false, true, false, true, false, true, false, false {
Push 0.0
InitArray
StoreRegister 2
Pop
Push register2, 0.0, 0.0
InitArray
SetMember
Push register2
Enumerate2
loc003f:StoreRegister 0
Push null
Equals2
If loc00d4
Push register0
StoreRegister 3
Pop
Push register3
Enumerate2
loc005d:StoreRegister 0
Push null
Equals2
If loc00aa
Push register0
StoreRegister 1
Pop
Push register1, 5
Equals2
Not
If loc00a5
loc0086:Push null
Equals2
Not
If loc0086
loc0091:Push null
Equals2
Not
If loc0091
Push 5
Return
loc00a5:Jump loc005d
loc00aa:Push register3, 8
Equals2
Not
If loc00cf
loc00bb:Push null
Equals2
Not
If loc00bb
Push 3
Return
loc00cf:Jump loc003f
loc00d4:Push 8
Return
}
Push "forInInTest"
Trace
Push 0.0, "tst"
CallFunction
Pop
