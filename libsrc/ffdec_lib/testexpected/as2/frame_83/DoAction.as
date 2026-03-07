ConstantPool "ifframeLoaded2Test", "A", "d", "B", "C", "D", "E"
Push "ifframeLoaded2Test"
Trace
Push "A"
Trace
WaitForFrame 9, 15
Push "d", 5
DefineLocal
Push "d"
GetVariable
Push 4
Equals2
Not
If loc0061
Push "B"
Trace
Jump loc0067
loc0061:Push "C"
Trace
loc0067:Push "D"
Trace
Push "E"
Trace
