ConstantPool "doWhile2Test", "k"
Push "doWhile2Test"
Trace
Push "k", 5
DefineLocal
loc0025:Push "k", "k"
GetVariable
Increment
SetVariable
Push "k"
GetVariable
Push 7
Equals2
Not
If loc0058
Push "k", 5, "k"
GetVariable
Multiply
SetVariable
Jump loc0067
loc0058:Push "k", 5, "k"
GetVariable
Add2
SetVariable
loc0067:Push "k"
GetVariable
Push 9
Less2
If loc0025
