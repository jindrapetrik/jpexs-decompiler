ConstantPool "commaOperator3Test", "k", "end"
Push "commaOperator3Test"
Trace
Push "k", 1
DefineLocal
loc002f:Push "k"
GetVariable
Push "k", "k"
GetVariable
Increment
SetVariable
Pop
Push "k"
GetVariable
Push 10
Less2
Not
If loc0073
Push "k", "k"
GetVariable
Push 5
Multiply
SetVariable
Push "k"
GetVariable
Trace
Jump loc002f
loc0073:Push "end"
Trace
