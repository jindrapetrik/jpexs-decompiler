ConstantPool "bagr", "_locy_", "r1", ". ", "r2", "v1", "unk", "chainedAfterForInTest"
DefineFunction2 "f", 0, 5, false, false, true, false, true, false, true, false, false {
Push 5
StoreRegister 4
Pop
Push 0.0
InitObject
StoreRegister 3
Pop
Push "bagr"
StoreRegister 2
Pop
Push "_locy_"
GetVariable
Enumerate2
loc0073:StoreRegister 0
Push null
Equals2
If loc0096
Push register0
StoreRegister 1
Pop
Push register1
Trace
Jump loc0073
loc0096:Push register3, "r1", register2, 1
Add2
Push ". "
Add2
Push register4
If loc00e1
Push register3, "r2", "v1"
GetVariable
Push register2
GetMember
Push 0.0
GetMember
StoreRegister 0
SetMember
Push register0
Jump loc00e6
loc00e1:Push "unk"
loc00e6:Add2
SetMember
}
Push "chainedAfterForInTest"
Trace
Push "v1", 0.0
InitObject
DefineLocal
