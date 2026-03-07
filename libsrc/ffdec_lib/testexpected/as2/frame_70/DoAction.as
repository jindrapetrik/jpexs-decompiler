ConstantPool "forWithContinue2Test", "s", "A", "i", "B", "C", "D", "j", "E"
Push "forWithContinue2Test"
Trace
Push "s", "A"
DefineLocal
Push "i", 0.0
DefineLocal
loc0047:Push "i"
GetVariable
Push 10
Less2
Not
If loc00d3
Push "s"
GetVariable
Push "B"
Equals2
Not
If loc0085
Push "s"
GetVariable
Push "C"
Equals2
Not
If loc0085
Jump loc00c4
loc0085:Push "D"
Trace
Push "j", 0.0
DefineLocal
loc009a:Push "j"
GetVariable
Push 29
Less2
Not
If loc00c4
Push "E"
Trace
Push "j", "j"
GetVariable
Increment
SetVariable
Jump loc009a
loc00c4:Push "i", "i"
GetVariable
Increment
SetVariable
Jump loc0047
loc00d3: