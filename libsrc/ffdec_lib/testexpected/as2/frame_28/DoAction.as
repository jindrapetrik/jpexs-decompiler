ConstantPool "forWithContinueTest", "i", "hello:", "i==5", "hawk", "end of the loop"
Push "forWithContinueTest"
Trace
Push "i", 0.0
DefineLocal
loc0051:Push "i"
GetVariable
Push 10
Less2
Not
If loc00c0
Push "hello:", "i"
GetVariable
Add2
Trace
Push "i"
GetVariable
Push 5
Equals2
Not
If loc00ab
Push "i==5"
Trace
Push "i"
GetVariable
Push 7
Equals2
Not
If loc00a5
Jump loc00b1
loc00a5:Push "hawk"
Trace
loc00ab:Push "end of the loop"
Trace
loc00b1:Push "i", "i"
GetVariable
Increment
SetVariable
Jump loc0051
loc00c0: