ConstantPool "deleteTest", "obj", "a", "b", "salam likum", "bagr aa"
Push "deleteTest"
Trace
Push "obj", "a", 1, "b", 2, 2
InitObject
DefineLocal
Push "obj"
GetVariable
Push "salam likum", 58
SetMember
Push "obj"
GetVariable
Push "a"
Delete
Pop
Push "obj"
GetVariable
Push "salam likum"
Delete
Pop
Push "bagr aa"
Delete2
Pop
