ConstantPool "tryTypeTest", "a", "err:", "e", "MyError", "e1", "e2", "Error"
Push "tryTypeTest"
Trace
Push "a", 5
DefineLocal
Try "e" {
Push "a", "a"
GetVariable
Push 0.0
Divide
SetVariable
Jump loc0070
}
Catch {
Push "err:", "e"
GetVariable
Add2
Trace
}
loc0070:Try "e" {
Push "a", "a"
GetVariable
Push 0.0
Divide
SetVariable
Jump loc00c1
}
Catch {
Push "a"
GetVariable
Push 0.0
Equals2
Not
If loc00b7
Push "e"
GetVariable
Throw
loc00b7:Push "err:", "e"
GetVariable
Add2
Trace
}
loc00c1:Try register0 {
Push "a", "a"
GetVariable
Push 0.0
Divide
SetVariable
Jump loc011b
}
Catch {
Push "MyError"
GetVariable
Push register0
CastOp
PushDuplicate
Push null
Equals2
If loc0114
Push "e"
StackSwap
DefineLocal
Push "err:", "e"
GetVariable
Add2
Trace
Jump loc011b
loc0114:Pop
Push register0
Throw
}
loc011b:Try register0 {
Push "a", "a"
GetVariable
Push 0.0
Divide
SetVariable
Jump loc0187
}
Catch {
Push "MyError"
GetVariable
Push register0
CastOp
PushDuplicate
Push null
Equals2
If loc016e
Push "e1"
StackSwap
DefineLocal
Push "err:", "e1"
GetVariable
Add2
Trace
Jump loc0187
loc016e:Pop
Push register0, "e2"
StackSwap
DefineLocal
Push "err:", "e2"
GetVariable
Add2
Trace
Jump loc0187
}
loc0187:Try register0 {
Push "a", "a"
GetVariable
Push 0.0
Divide
SetVariable
Jump loc020f
}
Catch {
Push "MyError"
GetVariable
Push register0
CastOp
PushDuplicate
Push null
Equals2
If loc01da
Push "e"
StackSwap
DefineLocal
Push "err:", "e"
GetVariable
Add2
Trace
Jump loc020f
loc01da:Pop
Push "Error"
GetVariable
Push register0
CastOp
PushDuplicate
Push null
Equals2
If loc0208
Push "e2"
StackSwap
DefineLocal
Push "err:", "e2"
GetVariable
Add2
Trace
Jump loc020f
loc0208:Pop
Push register0
Throw
}
loc020f: