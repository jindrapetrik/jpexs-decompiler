ConstantPool "unaryOpTest", "a", "c", "d", "e"
Push "unaryOpTest"
Trace
Push "a", 5
DefineLocal
Push "c", "a"
GetVariable
Push 4294967295.0
BitXor
DefineLocal
Push "d", "a"
GetVariable
Push "c"
GetVariable
Add2
Push 4294967295.0
BitXor
DefineLocal
Push "e", 0.0, "c"
GetVariable
Subtract
DefineLocal
