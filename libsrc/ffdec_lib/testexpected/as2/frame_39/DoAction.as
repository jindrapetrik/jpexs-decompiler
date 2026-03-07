ConstantPool "whileAndTest", "a", "b"
Push "whileAndTest"
Trace
Push "a", 5
DefineLocal
Push "b", 10
DefineLocal
loc0032:Push "a"
GetVariable
Push 10
Less2
PushDuplicate
Not
If loc0058
Pop
Push "b"
GetVariable
Push 1
Greater
loc0058:Not
If loc0077
Push "a", "a"
GetVariable
Increment
SetVariable
Push "b", "b"
GetVariable
Decrement
SetVariable
Jump loc0032
loc0077:Push "a", 7
SetVariable
Push "b", 9
SetVariable
