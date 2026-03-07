ConstantPool "switchAndTest", "a", "b", "a 0-3", "a 4-6", "a 7-10", "a 11-20", "a 0, b xx"
Push "switchAndTest"
Trace
Push "a", 5
DefineLocal
Push "b", 3
DefineLocal
Push true
StoreRegister 0
Push "a"
GetVariable
Push 0.0
Less2
Not
PushDuplicate
Not
If loc008d
Pop
Push "a"
GetVariable
Push 3
Greater
Not
loc008d:StrictEquals
If loc016b
Push register0, "a"
GetVariable
Push 4
Less2
Not
PushDuplicate
Not
If loc00bd
Pop
Push "a"
GetVariable
Push 6
Greater
Not
loc00bd:StrictEquals
If loc0176
Push register0, "a"
GetVariable
Push 7
Less2
Not
PushDuplicate
Not
If loc00ed
Pop
Push "a"
GetVariable
Push 10
Greater
Not
loc00ed:StrictEquals
If loc017c
Push register0, "a"
GetVariable
Push 11
Less2
Not
PushDuplicate
Not
If loc011d
Pop
Push "a"
GetVariable
Push 20
Greater
Not
loc011d:StrictEquals
If loc0187
Push register0, "a"
GetVariable
Push 0.0
Equals2
If loc0151
Push "b"
GetVariable
Push 5
Less2
Jump loc0160
loc0151:Push "b"
GetVariable
Push 5
Greater
loc0160:StrictEquals
If loc0192
Jump loc019d
loc016b:Push "a 0-3"
Trace
Jump loc019d
loc0176:Push "a 4-6"
Trace
loc017c:Push "a 7-10"
Trace
Jump loc019d
loc0187:Push "a 11-20"
Trace
Jump loc019d
loc0192:Push "a 0, b xx"
Trace
Jump loc019d
loc019d: