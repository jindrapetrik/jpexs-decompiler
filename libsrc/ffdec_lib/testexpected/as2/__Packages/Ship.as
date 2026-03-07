ConstantPool "_global", "Ship", "y", "prototype", "Moving", "moveUp", "moveUp = ", "moveDown", "moveDown = ", "moveLeft", "b", "moveRight", "a", "d", "ASSetPropFlags"
Push "_global"
GetVariable
Push "Ship"
GetMember
Not
Not
If loc01c3
Push "_global"
GetVariable
Push "Ship"
DefineFunction2 "", 1, 3, false, false, true, false, true, false, false, true, false, 2, "py" {
Push register1, "y", register2
SetMember
}
StoreRegister 1
SetMember
Push register1, "prototype"
GetMember
StoreRegister 2
Pop
Push "_global"
GetVariable
Push "Moving"
GetMember
Push 1, "_global"
GetVariable
Push "Ship"
GetMember
ImplementsOp
Push register2, "moveUp"
DefineFunction2 "", 1, 3, false, false, true, false, true, false, false, true, false, 2, "uy" {
Push register1, "y", register1, "y"
GetMember
Push register2
Multiply
SetMember
Push "moveUp = ", register1, "y"
GetMember
Add2
Trace
}
SetMember
Push register2, "moveDown"
DefineFunction2 "", 1, 3, false, false, true, false, true, false, false, true, false, 2, "dy" {
Push register1, "y", register1, "y"
GetMember
Push register2
Multiply
SetMember
Push "moveDown = ", register1, "y"
GetMember
Add2
Trace
}
SetMember
Push register2, "moveLeft"
DefineFunction2 "", 1, 2, false, false, true, false, true, false, false, true, false, 0, "lx" {
Push register1, "b", 6
SetMember
}
SetMember
Push register2, "moveRight"
DefineFunction2 "", 1, 2, false, false, true, false, true, false, false, true, false, 0, "rx" {
Push register1, "a"
GetMember
Trace
Push register1, "d"
GetMember
Trace
}
SetMember
Push register2, "d", 5
SetMember
Push 1, null, "_global"
GetVariable
Push "Ship"
GetMember
Push "prototype"
GetMember
Push 3, "ASSetPropFlags"
CallFunction
loc01c3:Pop
