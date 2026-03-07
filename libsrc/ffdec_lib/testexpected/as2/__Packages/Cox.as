ConstantPool "_global", "Cox", "Box", "prototype", "testPublic", "pub", "testPrivate", "priv", "ASSetPropFlags"
Push "_global"
GetVariable
Push "Cox"
GetMember
Not
Not
If loc0105
Push "_global"
GetVariable
Push "Cox"
DefineFunction2 "", 1, 3, false, false, false, true, true, false, true, false, false, 2, "passed_mc" {
Push register2, 1, register1, undefined
CallMethod
Pop
}
StoreRegister 1
SetMember
Push "_global"
GetVariable
Push "Cox"
GetMember
Push "Box"
GetVariable
Extends
Push register1, "prototype"
GetMember
StoreRegister 2
Pop
Push register2, "testPublic"
DefineFunction "", 0 {
Push "pub"
Trace
}
SetMember
Push register2, "testPrivate"
DefineFunction "", 0 {
Push "priv"
Trace
}
SetMember
Push 1, null, "_global"
GetVariable
Push "Cox"
GetMember
Push "prototype"
GetMember
Push 3, "ASSetPropFlags"
CallFunction
loc0105:Pop
