DefineFunction2 "testtry", 0, 2, false, false, true, false, true, false, true, false, false {
Push 5
StoreRegister 1
Pop
Try "e" {
Push register1, 3
Equals2
Not
If loc0041
Push undefined
Return
loc0041:Push register1, 4
Equals2
Not
If loc0067
Push 0.0, "Error"
NewObject
Throw
loc0067:Push 7
StoreRegister 1
Pop
Jump loc0084
}
Catch {
Push "error"
Trace
}
Finally {
loc0084:Push "finally"
Trace
}
}
Push "tryFunctionTest"
Trace
