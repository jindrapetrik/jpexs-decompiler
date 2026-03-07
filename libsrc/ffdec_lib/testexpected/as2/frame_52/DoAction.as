ConstantPool "commaOperatorTest", "a", "b", "c", "konec"
Push "commaOperatorTest"
Trace
Push "a", 0.0
DefineLocal
Push "b", 0.0
DefineLocal
Push "c", 0.0
DefineLocal
loc0056:Push "a"
GetVariable
Push "a", "a"
GetVariable
Increment
SetVariable
Pop
Push "b", "b"
GetVariable
Push 2
Add2
StoreRegister 0
SetVariable
Push register0
Pop
Push "c"
GetVariable
Push 10
Less2
Not
If loc00ae
Push "c"
GetVariable
Trace
Push "c", "c"
GetVariable
Increment
SetVariable
Jump loc0056
loc00ae:Push "konec"
Trace
