DefineFunction2 "myFunc", 0, 5, false, false, true, false, true, false, true, false, false {
Push 0.0
StoreRegister 2
Pop
Push 0.0
InitObject
StoreRegister 1
Pop
Push register1, register2, register2
Increment
StoreRegister 2
Pop
GetMember
StoreRegister 4
Pop
Push register1, register2, register2
Decrement
StoreRegister 2
Pop
GetMember
StoreRegister 3
Pop
}
Push "functionPostIncrementTest"
Trace
