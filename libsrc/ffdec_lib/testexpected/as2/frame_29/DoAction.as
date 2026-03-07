ConstantPool "doWhileTest", "i", "i=", "end"
Push "doWhileTest"
Trace
Push "i", 0.0
DefineLocal
loc002f:Push "i=", "i"
GetVariable
Add2
Trace
Push "i", "i"
GetVariable
Increment
SetVariable
Push "i"
GetVariable
Push 10
Less2
If loc002f
Push "end"
Trace
