ConstantPool "chainedAssignmentsTest", "a", "b", "c", "d"
Push "chainedAssignmentsTest"
Trace
Push "a", 7
DefineLocal
Push "b", 8
DefineLocal
Push "c", 9
DefineLocal
Push "d", "c", "b", "a", 10
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
Push "d"
GetVariable
Trace
