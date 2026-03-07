ConstantPool "_global", "MyError", "Error", "prototype", "message", "My custom error occurred", "ASSetPropFlags"
Push "_global"
GetVariable
Push "MyError"
GetMember
Not
Not
If loc00e1
Push "_global"
GetVariable
Push "MyError"
DefineFunction2 "", 0, 2, false, false, false, true, true, false, true, false, false {
Push 0.0, register1, undefined
CallMethod
Pop
}
StoreRegister 1
SetMember
Push "_global"
GetVariable
Push "MyError"
GetMember
Push "Error"
GetVariable
Extends
Push register1, "prototype"
GetMember
StoreRegister 2
Pop
Push register2, "message", "My custom error occurred"
SetMember
Push 1, null, "_global"
GetVariable
Push "MyError"
GetMember
Push "prototype"
GetMember
Push 3, "ASSetPropFlags"
CallFunction
loc00e1:Pop
