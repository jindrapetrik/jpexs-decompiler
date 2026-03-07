DefineFunction2 "myFunction", 1, 5, false, false, true, false, true, false, true, false, false, 1, "item" {
Push undefined
StoreRegister 2
Pop
Push undefined
StoreRegister 4
Pop
Push register1, 1, "isNaN"
CallFunction
Not
Not
If loc014f
Push 3, 2, 1, 3
InitArray
StoreRegister 2
Pop
Push register2
Enumerate2
loc0069:StoreRegister 0
Push null
Equals2
If loc014a
Push register0
StoreRegister 3
Pop
Push register3
StoreRegister 0
Push "A"
StrictEquals
If loc00c5
Push register0, "B"
StrictEquals
If loc00e5
Push register0, "C"
StrictEquals
If loc0105
Push register0, "D"
StrictEquals
If loc0125
Jump loc0145
loc00c5:Push register1, "a"
Equals2
Not
If loc00e5
loc00d4:Push null
Equals2
Not
If loc00d4
Push true
Return
loc00e5:Push register1, "b"
Equals2
Not
If loc0105
loc00f4:Push null
Equals2
Not
If loc00f4
Push true
Return
loc0105:Push register1, "c"
Equals2
Not
If loc0125
loc0114:Push null
Equals2
Not
If loc0114
Push true
Return
loc0125:Push register1, "d"
Equals2
Not
If loc0145
loc0134:Push null
Equals2
Not
If loc0134
Push true
Return
loc0145:Jump loc0069
loc014a:Jump loc0160
loc014f:Push "item is nan"
Trace
loc0160:Push false
Return
}
Push "breakDetectionTest"
Trace
