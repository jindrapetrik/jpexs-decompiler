ConstantPool "forInBreakTest", "obj", "a", "b", "c", "k", "after"
Push "forInBreakTest"
Trace
Push "obj", "a", 5, "b", 6, "c", 7, 3
InitObject
DefineLocal
Push "obj"
GetVariable
Enumerate2
StoreRegister 0
Push null
Equals2
If loc0081
Push "k", register0
DefineLocal
Push "k"
GetVariable
Trace
Jump loc0076
loc0076:Push null
Equals2
Not
If loc0076
loc0081:Push "after"
Trace
