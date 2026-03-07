ConstantPool "doWhileTwiceTest", "a", "b", "x", "y", "z", "g", "h", "finish"
Push "doWhileTwiceTest"
Trace
Push "a", 1
DefineLocal
Push "b", 2
DefineLocal
loc0047:Push "a"
GetVariable
Not
If loc0070
Push "x"
Trace
Push "b"
GetVariable
Not
If loc006a
Jump loc0080
loc006a:Push "y"
Trace
loc0070:Push "z"
Trace
Push true
If loc0047
loc0080:Push "g"
Trace
Push "b"
GetVariable
Not
If loc0097
Jump loc00a7
loc0097:Push "h"
Trace
Push true
If loc0047
loc00a7:Push "finish"
Trace
