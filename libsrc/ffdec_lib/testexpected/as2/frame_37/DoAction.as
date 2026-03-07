ConstantPool "objectsTest", "flashBox", "box1", "Box", "_root", "onEnterFrame", "moveUp", "ship", "Ship", "enemy", "Enemy", "moveDown", "moveRight", "moveLeft", "c", "Cox"
Push "objectsTest"
Trace
Push "flashBox", "box1"
GetVariable
Push 1, "Box"
NewObject
DefineLocal
Push "_root"
GetVariable
Push "onEnterFrame"
DefineFunction "", 0 {
Push 0.0, "flashBox"
GetVariable
Push "moveUp"
CallMethod
Pop
}
SetMember
Push "ship", 200, 1, "Ship"
NewObject
DefineLocal
Push "enemy", 56, 1, "Enemy"
NewObject
DefineLocal
Push 0.5, 1, "ship"
GetVariable
Push "moveDown"
CallMethod
Pop
Push 0.2, 1, "ship"
GetVariable
Push "moveUp"
CallMethod
Pop
Push 230, 1, "enemy"
GetVariable
Push "moveRight"
CallMethod
Pop
Push 100, 1, "enemy"
GetVariable
Push "moveLeft"
CallMethod
Pop
Push "c", "box1"
GetVariable
Push 1, "Cox"
NewObject
DefineLocal
