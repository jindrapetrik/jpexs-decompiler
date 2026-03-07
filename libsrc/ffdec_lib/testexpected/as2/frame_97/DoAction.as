ConstantPool "twoInTest", "o", "a", "n", "c", "i", "xx"
Push "twoInTest"
Trace
Push "o", "a", 0.0
InitObject
Push 1
InitObject
DefineLocal
Push "o"
GetVariable
Enumerate2
loc0044:StoreRegister 0
Push null
Equals2
If loc00d4
Push "n", register0
DefineLocal
Push "c", 5
DefineLocal
Push "o"
GetVariable
Push "a"
GetMember
Enumerate2
loc0072:StoreRegister 0
Push null
Equals2
If loc00cf
Push "i", register0
DefineLocal
Push "i"
GetVariable
Push "c"
GetVariable
Equals2
Not
If loc00bf
Push "i"
GetVariable
Push 0.0
Equals2
Not
If loc00bf
Push "xx"
Trace
Jump loc00c4
loc00bf:Jump loc0072
loc00c4:Push null
Equals2
Not
If loc00c4
loc00cf:Jump loc0044
loc00d4: