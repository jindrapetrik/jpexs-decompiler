ConstantPool "tryInsideForInTest", "obj", "thing", "a", "Object", "error"
Push "tryInsideForInTest"
Trace
Push "obj", 0.0
InitObject
DefineLocal
Push "obj"
GetVariable
Enumerate2
loc004e:StoreRegister 0
Push null
Equals2
If loc00a9
Push "thing", register0
DefineLocal
Try register0 {
Push "a"
Trace
Jump loc00a4
}
Catch {
Push "Object"
GetVariable
Push register0
CastOp
PushDuplicate
Push null
Equals2
If loc009d
Push "error"
StackSwap
DefineLocal
Jump loc00a4
loc009d:Pop
Push register0
Throw
}
loc00a4:Jump loc004e
loc00a9: