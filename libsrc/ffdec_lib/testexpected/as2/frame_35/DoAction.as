ConstantPool "multipleConditions2Test", "k", "first", "second", "finish"
Push "multipleConditions2Test"
Trace
Push "k", 5
DefineLocal
Push "k"
GetVariable
Push 7
Equals2
PushDuplicate
Not
If loc006a
Pop
Push "k"
GetVariable
Push 8
Equals2
loc006a:Not
If loc0076
Push "first"
Trace
loc0076:Push "k"
GetVariable
Push 9
Equals2
PushDuplicate
If loc009b
Pop
Push "k"
GetVariable
Push 6
Equals2
loc009b:Not
If loc00a7
Push "second"
Trace
loc00a7:Push "finish"
Trace
