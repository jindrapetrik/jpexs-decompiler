ConstantPool "strictEqualsTest", "i", "equals strict", "not equals strict"
Push "strictEqualsTest"
Trace
Push "i", 5
DefineLocal
Push "i"
GetVariable
Push 5
StrictEquals
Not
If loc0064
Push "equals strict"
Trace
loc0064:Push "i"
GetVariable
Push 5
StrictEquals
Not
Not
If loc0080
Push "not equals strict"
Trace
loc0080: