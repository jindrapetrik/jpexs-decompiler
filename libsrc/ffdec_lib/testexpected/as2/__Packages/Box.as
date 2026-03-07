ConstantPool "_global", "Box", "box_mc", "MovieClip", "prototype", "moveUp", "_y", "moveDown", "ASSetPropFlags"
Push "_global"
GetVariable
Push "Box"
GetMember
Not
Not
If loc017b
Push "_global"
GetVariable
Push "Box"
DefineFunction2 "", 1, 4, false, false, false, true, true, false, false, true, false, 3, "passed_mc" {
Push 0
Push register2
Push undefined
CallMethod
Pop
Push register1
Push "box_mc"
Push register3
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
Push register1
Push "prototype"
GetMember
StoreRegister 2
Pop
Push register2
Push "moveUp"
DefineFunction2 "", 0, 2, false, false, true, false, true, false, false, true, false {
Push register1
Push "box_mc"
GetMember
Push "_y"
Push register1
Push "box_mc"
GetMember
Push "_y"
GetMember
Push 1
Subtract
SetMember
}
SetMember
Push register2
Push "moveDown"
DefineFunction2 "", 0, 2, false, false, true, false, true, false, false, true, false {
Push register1
Push "box_mc"
GetMember
Push "_y"
Push register1
Push "box_mc"
GetMember
Push "_y"
GetMember
Push 20
Add2
SetMember
}
SetMember
Push 1
Push null
Push "_global"
GetVariable
Push "Box"
GetMember
Push "prototype"
GetMember
Push 3
Push "ASSetPropFlags"
CallFunction
loc017b:Pop
