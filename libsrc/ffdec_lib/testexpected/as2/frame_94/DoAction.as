DefineFunction "reset", 0 {
}
Push "functionSwitchTest"
Trace
Stop
Push "test"
GetVariable
StoreRegister 0
Push 1
StrictEquals
If loc0067
Push register0, 2
StrictEquals
If loc0073
Push register0, 3
StrictEquals
If loc0073
Jump loc007a
loc0067:Push "A"
Trace
Jump loc007a
loc0073:Push "B"
Trace
loc007a: