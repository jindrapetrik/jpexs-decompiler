ConstantPool "switchForTest", "i", "zero", "five", "ten", "one", "def", "before loop end"
Push "switchForTest"
Trace
Push "i", 0.0
DefineLocal
loc0050:Push "i"
GetVariable
Push 10
Less2
Not
If loc0112
Push "i"
GetVariable
StoreRegister 0
Push 0.0
StrictEquals
If loc00b6
Push register0, 5
StrictEquals
If loc00c1
Push register0, 10
StrictEquals
If loc00cc
Push register0, 1
StrictEquals
If loc00d7
Jump loc00f7
loc00b6:Push "zero"
Trace
Jump loc0103
loc00c1:Push "five"
Trace
Jump loc00fd
loc00cc:Push "ten"
Trace
Jump loc00fd
loc00d7:Push "i"
GetVariable
Push 7
Equals2
Not
If loc00f1
Jump loc0103
loc00f1:Push "one"
Trace
loc00f7:Push "def"
Trace
loc00fd:Push "before loop end"
Trace
loc0103:Push "i", "i"
GetVariable
Increment
SetVariable
Jump loc0050
loc0112: