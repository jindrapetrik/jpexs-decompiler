ConstantPool "commaOperator2Test", "k", "h", "f", "b", "gg", "ss"
Push "commaOperator2Test"
Trace
Push "k", 8
DefineLocal
loc0037:Push "k"
GetVariable
Push 9
Equals2
Not
If loc0078
Push "h"
Trace
Push "k"
GetVariable
Push 9
Equals2
Not
If loc0072
Push "f"
Trace
Jump loc007e
loc0072:Push "b"
Trace
loc0078:Push "gg"
Trace
loc007e:Push "k"
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
If loc0037
Push "ss"
Trace
