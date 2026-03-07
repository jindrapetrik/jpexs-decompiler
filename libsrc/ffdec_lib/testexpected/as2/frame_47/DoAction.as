ConstantPool "ternarTest", "a", "b"
Push "ternarTest"
Trace
Push "a", 5
DefineLocal
Push "b", "a"
GetVariable
Push 4
Equals2
If loc0048
Push 3
Jump loc0050
loc0048:Push 2
loc0050:DefineLocal
Push "b"
GetVariable
Trace
