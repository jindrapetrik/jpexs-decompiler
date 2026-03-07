ConstantPool "switchTest", "i", "one", "two", "three", "four", "default clause", "scriptend"
Push "switchTest"
Trace
Push "i", 5
DefineLocal
Push "i"
GetVariable
StoreRegister 0
Push 0.0
StrictEquals
If loc00b0
Push register0, 1
StrictEquals
If loc00b0
Push register0, 2
StrictEquals
If loc00bb
Push register0, 3
StrictEquals
If loc00c1
Push register0, 4
StrictEquals
If loc00cc
Jump loc00d7
loc00b0:Push "one"
Trace
Jump loc00dd
loc00bb:Push "two"
Trace
loc00c1:Push "three"
Trace
Jump loc00dd
loc00cc:Push "four"
Trace
Jump loc00dd
loc00d7:Push "default clause"
Trace
loc00dd:Push "scriptend"
Trace
