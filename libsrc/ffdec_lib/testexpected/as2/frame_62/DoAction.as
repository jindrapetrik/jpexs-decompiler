ConstantPool "switchDefaultTest2", "k", "5", "default", "default, 6", "7", "afterSwitch"
Push "switchDefaultTest2"
Trace
Push "k", 5
DefineLocal
Push "k"
GetVariable
StoreRegister 0
Push 5
StrictEquals
If loc008b
Push register0, 6
StrictEquals
If loc009c
Push register0, 7
StrictEquals
If loc00a7
Jump loc0096
loc008b:Push "5"
Trace
Jump loc00ad
loc0096:Push "default"
Trace
loc009c:Push "default, 6"
Trace
Jump loc00ad
loc00a7:Push "7"
Trace
loc00ad:Push "afterSwitch"
Trace
