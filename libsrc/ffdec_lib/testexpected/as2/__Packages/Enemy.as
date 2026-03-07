ConstantPool "_global", "Enemy", "x", "prototype", "Moving", "sfunc", "hu", "moveLeft", "tst", "moveLeft = ", "moveRight", "moveRight = ", "moveUp", "moveDown", "stat_tst", "ASSetPropFlags"
Push "_global"
GetVariable
Push "Enemy"
GetMember
Not
Not
If loc020a
Push "_global"
GetVariable
Push "Enemy"
DefineFunction2 "", 1, 5, false, false, true, false, true, false, false, true, false, 4, "px" {
Push 57
StoreRegister 2
Pop
Push register2, 27
Multiply
StoreRegister 2
Pop
Push register2
StoreRegister 3
Pop
Push register1, "x", register4, register3
Add2
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
Push "Enemy"
GetMember
ImplementsOp
Push register1, "sfunc"
DefineFunction "", 0 {
Push "hu"
Trace
}
SetMember
Push register2, "moveLeft"
DefineFunction2 "", 1, 3, false, false, true, false, true, false, false, true, false, 2, "lx" {
Push register1, "x", register1, "x"
GetMember
Push register2
Subtract
SetMember
Push register1, "tst", 7
SetMember
Push "moveLeft = ", register1, "x"
GetMember
Add2
Trace
}
SetMember
Push register2, "moveRight"
DefineFunction2 "", 1, 3, false, false, true, false, true, false, false, true, false, 2, "rx" {
Push register1, "x", register1, "x"
GetMember
Push register2
Add2
SetMember
Push "moveRight = ", register1, "x"
GetMember
Add2
Trace
}
SetMember
Push register2, "moveUp"
DefineFunction "", 1, "uy"  {
}
SetMember
Push register2, "moveDown"
DefineFunction "", 1, "dy"  {
}
SetMember
Push register2, "tst", 5
SetMember
Push register1, "stat_tst", 6
SetMember
Push 1, null, "_global"
GetVariable
Push "Enemy"
GetMember
Push "prototype"
GetMember
Push 3, "ASSetPropFlags"
CallFunction
loc020a:Pop
