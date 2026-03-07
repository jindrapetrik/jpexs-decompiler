ConstantPool "switchDefaultTest", "k", "default 5", "default 5,6", "7", "afterSwitch"
Push "switchDefaultTest"
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
If loc0091
Push register0, 7
StrictEquals
If loc009c
Jump loc008b
loc008b:Push "default 5"
Trace
loc0091:Push "default 5,6"
Trace
Jump loc00a2
loc009c:Push "7"
Trace
loc00a2:Push "afterSwitch"
Trace
