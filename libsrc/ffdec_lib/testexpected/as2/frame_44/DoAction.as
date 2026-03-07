ConstantPool "chainedAssignments2Test", "a", "b", "c", "d", "i"
Push "chainedAssignments2Test"
Trace
Push "a", 5
DefineLocal
Push "b", 6
DefineLocal
Push "c", 7
DefineLocal
Push "d", "c", "b", "a", 4
StoreRegister 0
SetVariable
Push register0
StoreRegister 0
SetVariable
Push register0
StoreRegister 0
SetVariable
Push register0
DefineLocal
Push "d", "c", "b", "a", 7
StoreRegister 0
SetVariable
Push register0
StoreRegister 0
SetVariable
Push register0
StoreRegister 0
SetVariable
Push register0
StoreRegister 0
SetVariable
Push register0, 2
Greater
Not
If loc00c8
Push "d"
GetVariable
Trace
loc00c8:Push "d"
GetVariable
Push 1
Add2
Trace
Push "i", 0.0
DefineLocal
loc00e7:Push "i"
GetVariable
Push 5
Less2
Not
If loc0125
Push "i"
GetVariable
Push 7
Equals2
Not
If loc0116
Jump loc0116
loc0116:Push "i", "i"
GetVariable
Increment
SetVariable
Jump loc00e7
loc0125: