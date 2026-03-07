ConstantPool "ifWithElseTest", "i", "onTrue", "onFalse"
Push "ifWithElseTest"
Trace
Push "i", 5
DefineLocal
Push "i"
GetVariable
Push 258
Equals2
Not
If loc0056
Push "onTrue"
Trace
Jump loc0060
loc0056:Push "onFalse", "i"
GetVariable
Add2
Trace
loc0060: