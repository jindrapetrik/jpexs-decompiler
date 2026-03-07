ConstantPool "multipleConditionsTest", "k", "first", "second", "finish"
Push "multipleConditionsTest"
Trace
Push "k", 5
DefineLocal
Push "k"
GetVariable
Push 7
Equals2
PushDuplicate
Not
If loc0069
Pop
Push "k"
GetVariable
Push 8
Equals2
loc0069:Not
If loc0075
Push "first"
Trace
loc0075:Push "k"
GetVariable
Push 9
Equals2
Not
If loc0090
Push "second"
Trace
loc0090:Push "finish"
Trace
