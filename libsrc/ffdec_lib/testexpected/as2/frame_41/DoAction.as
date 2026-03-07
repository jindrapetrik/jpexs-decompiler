ConstantPool "tryTest", "k", "bug ", "e", "huu", "next", "bug2 ", "next2", "final", "end"
Push "tryTest"
Trace
Push "k", 5
DefineLocal
Try "e" {
Push "k", Infinity
SetVariable
Jump loc0070
}
Catch {
Push "bug ", "e"
GetVariable
Add2
Trace
}
Finally {
loc0070:Push "huu"
Trace
}
Push "next"
Trace
Try "e" {
Push "k", 6
SetVariable
Jump loc00a2
}
Catch {
Push "bug2 ", "e"
GetVariable
Add2
Trace
}
loc00a2:Push "next2"
Trace
Push "k", 5
DefineLocal
Try {
Push "k", Infinity
SetVariable
}
Finally {
Push "final"
Trace
}
Push "end"
Trace
