ConstantPool "switchForInTest", "t", "a", "z", "y", "x", "A", "B", "k", "finish"
Push "switchForInTest"
Trace
Push "t", 5
DefineLocal
Push "a", "z", "y", "x", 3
InitArray
DefineLocal
Push "t"
GetVariable
StoreRegister 0
Push 0.0
StrictEquals
If loc0090
Push register0, 1
StrictEquals
If loc009b
Push register0, 2
StrictEquals
If loc00a6
Jump loc00cf
loc0090:Push "A"
Trace
Jump loc00cf
loc009b:Push "B"
Trace
Jump loc00cf
loc00a6:Push "a"
GetVariable
Enumerate2
loc00ad:StoreRegister 0
Push null
Equals2
If loc00cf
Push "k", register0
DefineLocal
Push "k"
GetVariable
Trace
Jump loc00ad
loc00cf:Push "finish"
Trace
