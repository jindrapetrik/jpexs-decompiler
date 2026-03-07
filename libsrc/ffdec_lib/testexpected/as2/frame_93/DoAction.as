ConstantPool "testInnerSwitchNoLabel", "a", "b", "4", "7", "1", "2", "end"
Push "testInnerSwitchNoLabel"
Trace
Push "a"
DefineLocal2
Push "b"
DefineLocal2
Push "a"
GetVariable
StoreRegister 0
Push 4
StrictEquals
If loc006b
Push register0, 7
StrictEquals
If loc0076
Jump loc0081
loc006b:Push "4"
Trace
Jump loc00c4
loc0076:Push "7"
Trace
Jump loc00c4
loc0081:Push "b"
GetVariable
StoreRegister 0
Push 1
StrictEquals
If loc00ae
Push register0, 2
StrictEquals
If loc00b9
Jump loc00c4
loc00ae:Push "1"
Trace
Jump loc00c4
loc00b9:Push "2"
Trace
Jump loc00c4
loc00c4:Push "end"
Trace
