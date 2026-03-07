ConstantPool "forInTest", "testForIn", "arr", "a"
DefineFunction2 "testForIn", 0, 3, false, false, true, false, true, false, true, false, false {
Push 0.0
InitArray
StoreRegister 1
Pop
Push register1
Enumerate2
loc004b:StoreRegister 0
Push null
Equals2
If loc00b2
Push register0
StoreRegister 2
Pop
Push register2, 3
Greater
Not
If loc00ad
Push register2, 5
Equals2
Not
If loc0099
loc0085:Push null
Equals2
Not
If loc0085
Push 7
Return
loc0099:Push null
Equals2
Not
If loc0099
Push 8
Return
loc00ad:Jump loc004b
}
loc00b2:Push "forInTest"
Trace
Push 0.0, "testForIn"
CallFunction
Trace
Push "arr", 0.0
InitArray
DefineLocal
Push "arr"
GetVariable
Enumerate2
loc00df:StoreRegister 0
Push null
Equals2
If loc0101
Push "a", register0
DefineLocal
Push "a"
GetVariable
Trace
Jump loc00df
loc0101: