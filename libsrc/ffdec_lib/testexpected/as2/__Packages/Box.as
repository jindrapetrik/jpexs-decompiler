ConstantPool "_global", "Box", "box_mc", "MovieClip", "prototype", "moveUp", "_y", "moveDown", "ASSetPropFlags"
Push "_global"
GetVariable
Push "Box"
GetMember
Not
Not
If loc014b
Push "_global"
GetVariable
Push "Box"
DefineFunction2 "", 1, 4, false, false, false, true, true, false, false, true, false, 3, "passed_mc" {
Push 0, register2, undefined
CallMethod
Pop
Push register1, "box_mc", register3
SetMember
}
StoreRegister 1
SetMember
Push "_global"
GetVariable
Push "Box"
GetMember
Push "MovieClip"
GetVariable
Extends
Push register1, "prototype"
GetMember
StoreRegister 2
Pop
Push register2, "moveUp"
DefineFunction2 "", 0, 2, false, false, true, false, true, false, false, true, false {
Push register1, "box_mc"
GetMember
Push "_y", register1, "box_mc"
GetMember
Push "_y"
GetMember
Push 1
Subtract
SetMember
}
SetMember
Push register2, "moveDown"
DefineFunction2 "", 0, 2, false, false, true, false, true, false, false, true, false {
Push register1, "box_mc"
GetMember
Push "_y", register1, "box_mc"
GetMember
Push "_y"
GetMember
Push 20
Add2
SetMember
}
SetMember
Push 1, null, "_global"
GetVariable
Push "Box"
GetMember
Push "prototype"
GetMember
Push 3, "ASSetPropFlags"
CallFunction
loc014b:Pop
