DefineFunction2 "hello", 2, 3, false, false, true, false, true, false, true, false, false, 2, "what", 1, "second" {
Push "hello ", register2
Add2
Push "! "
Add2
Push register1
Add2
Trace
}
Push "functionTest"
Trace
Push 7, "friend", 2, "hello"
CallFunction
Pop
