ConstantPool "obj", "b", "found", "hi", "after", "c", "hello", "hohoho", "key1", "key2", "key3", "a", "loop1_break", "loop2_break", "loop2_inside", "after_loop2", "loop1_inside", "after_loop1", "forInBreakTest"
DefineFunction2 "testFunc1", 0, 2, false, false, true, false, true, false, true, false, false {
Push "obj"
GetVariable
Enumerate2
loc00ae:StoreRegister 0
Push null
Equals2
If loc00d1
Push register0
StoreRegister 1
Pop
Push register1
Trace
Jump loc00ae
}
loc00d1:DefineFunction2 "testFunc2", 0, 2, false, false, true, false, true, false, true, false, false {
Push "obj"
GetVariable
Enumerate2
loc00ec:StoreRegister 0
Push null
Equals2
If loc012d
Push register0
StoreRegister 1
Pop
Push register1, "b"
Equals2
Not
If loc011d
Push "found"
Trace
Jump loc0122
loc011d:Jump loc00ec
loc0122:Push null
Equals2
Not
If loc0122
}
loc012d:DefineFunction2 "testFunc3", 0, 2, false, false, true, false, true, false, true, false, false {
Push "obj"
GetVariable
Enumerate2
loc0148:StoreRegister 0
Push null
Equals2
If loc0189
Push register0
StoreRegister 1
Pop
Push register1, "b"
Equals2
Not
If loc0179
Push "hi"
Trace
Jump loc017e
loc0179:Jump loc0148
loc017e:Push null
Equals2
Not
If loc017e
loc0189:Push "after"
Trace
}
DefineFunction2 "testFunc4", 0, 2, false, false, true, false, true, false, true, false, false {
Push "obj"
GetVariable
Enumerate2
loc01aa:StoreRegister 0
Push null
Equals2
If loc020a
Push register0
StoreRegister 1
Pop
Push register1, "b"
Equals2
Not
If loc01db
Push "hi"
Trace
Jump loc01ff
loc01db:Push register1, "c"
Equals2
Not
If loc01f4
Push "hello"
Trace
Jump loc01ff
loc01f4:Push "hohoho"
Trace
Jump loc01aa
loc01ff:Push null
Equals2
Not
If loc01ff
loc020a:Push "after"
Trace
}
DefineFunction2 "testFunc5", 0, 7, false, false, true, false, true, false, true, false, false {
Push "key1", 1, "key2", 2, "key3", 3, 3
InitObject
StoreRegister 1
Pop
Push "obj"
GetVariable
Enumerate2
loc024e:StoreRegister 0
Push null
Equals2
If loc02fc
Push register0
StoreRegister 3
Pop
Push register3, "a"
Equals2
Not
If loc027f
Push "loop1_break"
Trace
Jump loc02f1
loc027f:Push register3, "b"
Equals2
Not
If loc02e6
Push "hello"
Trace
Push register1
Enumerate2
loc0299:StoreRegister 0
Push null
Equals2
If loc02e0
Push register0
StoreRegister 2
Pop
Push register2, "key1"
Equals2
Not
If loc02ca
Push "loop2_break"
Trace
Jump loc02d5
loc02ca:Push "loop2_inside"
Trace
Jump loc0299
loc02d5:Push null
Equals2
Not
If loc02d5
loc02e0:Push "after_loop2"
Trace
loc02e6:Push "loop1_inside"
Trace
Jump loc024e
loc02f1:Push null
Equals2
Not
If loc02f1
loc02fc:Push "after_loop1"
Trace
}
DefineFunction2 "testFunc6", 0, 7, false, false, true, false, true, false, true, false, false {
Push "key1", 1, "key2", 2, "key3", 3, 3
InitObject
StoreRegister 1
Pop
Push "obj"
GetVariable
Enumerate2
loc0340:StoreRegister 0
Push null
Equals2
If loc03da
Push register0
StoreRegister 3
Pop
Push register3, "a"
Equals2
Not
If loc0371
Push "loop1_break"
Trace
Jump loc03cf
loc0371:Push "hello"
Trace
Push register1
Enumerate2
loc037d:StoreRegister 0
Push null
Equals2
If loc03c4
Push register0
StoreRegister 2
Pop
Push register2, "key1"
Equals2
Not
If loc03ae
Push "loop2_break"
Trace
Jump loc03b9
loc03ae:Push "loop2_inside"
Trace
Jump loc037d
loc03b9:Push null
Equals2
Not
If loc03b9
loc03c4:Push "after_loop2"
Trace
Jump loc0340
loc03cf:Push null
Equals2
Not
If loc03cf
loc03da:Push "after_loop1"
Trace
}
Push "forInBreakTest"
Trace
Push "obj", "a", 5, "b", 6, "c", 7, 3
InitObject
DefineLocal
