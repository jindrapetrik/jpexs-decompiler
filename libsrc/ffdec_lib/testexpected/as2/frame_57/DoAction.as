ConstantPool "commaOperator4Test", "k", "a", "d", "b", "end"
Push "commaOperator4Test"
Trace
Push "k", 0.0
DefineLocal
loc0039:Push "k"
GetVariable
Trace
Push "k"
GetVariable
Push 8
Equals2
Not
If loc0081
Push "a"
Trace
Push "k"
GetVariable
Push 9
Equals2
Not
If loc0075
Jump loc008b
loc0075:Push "d"
Trace
Push "b"
Trace
loc0081:Push "k", "k"
GetVariable
Increment
SetVariable
loc008b:Push "k", "k"
GetVariable
Push 5
Add2
StoreRegister 0
SetVariable
Push register0
Pop
Push "k"
GetVariable
Push 20
Less2
If loc0039
Push "end"
Trace
