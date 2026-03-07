ConstantPool "numbersTest", "x", "null:", "true:", "false:", "1:", "0x7fffffff:", "0x80000000:", "-0x80000000:", "-0x80000001:"
Push "numbersTest"
Trace
Push "x", null
DefineLocal
Push "null:", "x"
GetVariable
Add2
Trace
Push "x", true
SetVariable
Push "true:", "x"
GetVariable
Add2
Trace
Push "x", false
SetVariable
Push "false:", "x"
GetVariable
Add2
Trace
Push "x", 1
SetVariable
Push "1:", "x"
GetVariable
Add2
Trace
Push "x", 2147483647
SetVariable
Push "0x7fffffff:", "x"
GetVariable
Add2
Trace
Push "x", 2147483648.0
SetVariable
Push "0x80000000:", "x"
GetVariable
Add2
Trace
Push "x", -2147483648
SetVariable
Push "-0x80000000:", "x"
GetVariable
Add2
Trace
Push "x", -2147483649
SetVariable
Push "-0x80000001:", "x"
GetVariable
Add2
Trace
