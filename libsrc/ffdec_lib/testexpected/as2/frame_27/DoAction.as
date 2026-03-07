ConstantPool "whileTest", "i", "hello:"
Push "whileTest"
Trace
Push "i", 0.0
DefineLocal
loc002d:Push "i"
GetVariable
Push 10
Less2
Not
If loc005b
Push "hello:", "i"
GetVariable
Add2
Trace
Push "i", "i"
GetVariable
Increment
SetVariable
Jump loc002d
loc005b: