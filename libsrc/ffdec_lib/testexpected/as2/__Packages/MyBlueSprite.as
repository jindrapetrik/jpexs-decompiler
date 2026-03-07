ConstantPool "_global", "MyBlueSprite", "MovieClip", "prototype", "ASSetPropFlags"
Push "_global"
GetVariable
Push "MyBlueSprite"
GetMember
Not
Not
If loc00bf
Push "_global"
GetVariable
Push "MyBlueSprite"
DefineFunction2 "", 0, 2, false, false, false, true, true, false, true, false, false {
Push 0.0, register1, undefined
CallMethod
Pop
}
StoreRegister 1
SetMember
Push "_global"
GetVariable
Push "MyBlueSprite"
GetMember
Push "MovieClip"
GetVariable
Extends
Push register1, "prototype"
GetMember
StoreRegister 2
Pop
Push 1, null, "_global"
GetVariable
Push "MyBlueSprite"
GetMember
Push "prototype"
GetMember
Push 3, "ASSetPropFlags"
CallFunction
loc00bf:Pop
