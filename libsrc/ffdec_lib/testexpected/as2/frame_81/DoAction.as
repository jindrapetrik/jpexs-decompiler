ConstantPool "globalFuncAsVarTest", "trace", "t"
DefineFunction2 "t", 1, 2, false, false, true, false, true, false, true, false, false, 1, "x" {
Push register1
Trace
}
Push "globalFuncAsVarTest"
Trace
Push "trace", 5
DefineLocal
Push "trace", "t"
GetVariable
SetVariable
