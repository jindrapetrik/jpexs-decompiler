ConstantPool "forTest", "i", "hello:"
Push "forTest"
Trace
Push "i", 0.0
DefineLocal
loc002b:Push "i"
GetVariable
Push 10
Less2
Not
If loc0059
Push "hello:", "i"
GetVariable
Add2
Trace
Push "i", "i"
GetVariable
Increment
SetVariable
Jump loc002b
loc0059: