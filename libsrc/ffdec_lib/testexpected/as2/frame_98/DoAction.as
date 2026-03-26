ConstantPool "andIntTest", "a", "b", "okay"
Push "andIntTest"
Trace
Push "a", 1
DefineLocal
Push "b", 5
DefineLocal
Push 0.0
PushDuplicate
Not
If loc0065
Pop
Push 1
PushDuplicate
If loc0065
Pop
Push "a"
GetVariable
Push "b"
GetVariable
Less2
loc0065:Not
If loc0071
Push "okay"
Trace
loc0071: