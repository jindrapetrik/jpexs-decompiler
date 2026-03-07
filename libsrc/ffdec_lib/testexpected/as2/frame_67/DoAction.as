ConstantPool "place1", "place2", "place3", "after switch", "switchVariantsTest"
DefineFunction2 "test1", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0084
Push register0, 2
StrictEquals
If loc008a
Jump loc0090
loc0084:Push "place1"
Trace
loc008a:Push "place2"
Trace
loc0090:Push "place3"
Trace
Push "after switch"
Trace
}
DefineFunction2 "test2", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc00f6
Push register0, 2
StrictEquals
If loc00f6
Push register0, 3
StrictEquals
If loc00f6
Jump loc00fc
loc00f6:Push "place3"
Trace
loc00fc:Push "after switch"
Trace
}
DefineFunction2 "test3", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc015c
Push register0, 2
StrictEquals
If loc0162
Push register0, 3
StrictEquals
If loc0168
Jump loc016e
loc015c:Push "place1"
Trace
loc0162:Push "place2"
Trace
loc0168:Push "place3"
Trace
loc016e:Push "after switch"
Trace
}
DefineFunction2 "test4", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc01be
Push register0, 2
StrictEquals
If loc01c9
Jump loc01cf
loc01be:Push "place1"
Trace
Jump loc01d5
loc01c9:Push "place2"
Trace
loc01cf:Push "place3"
Trace
loc01d5:Push "after switch"
Trace
}
DefineFunction2 "test5", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0235
Push register0, 2
StrictEquals
If loc0240
Push register0, 3
StrictEquals
If loc0246
Jump loc024c
loc0235:Push "place1"
Trace
Jump loc024c
loc0240:Push "place2"
Trace
loc0246:Push "place3"
Trace
loc024c:Push "after switch"
Trace
}
DefineFunction2 "test6", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc029c
Push register0, 2
StrictEquals
If loc02a2
Jump loc02a2
loc029c:Push "place1"
Trace
loc02a2:Push "place3"
Trace
Push "after switch"
Trace
}
DefineFunction2 "test7", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc02f8
Push register0, 2
StrictEquals
If loc02fe
Jump loc0309
loc02f8:Push "place1"
Trace
loc02fe:Push "place2"
Trace
Jump loc030f
loc0309:Push "place3"
Trace
loc030f:Push "after switch"
Trace
}
DefineFunction2 "test8", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc036f
Push register0, 2
StrictEquals
If loc036f
Push register0, 3
StrictEquals
If loc0375
Jump loc037b
loc036f:Push "place2"
Trace
loc0375:Push "place3"
Trace
loc037b:Push "after switch"
Trace
}
DefineFunction2 "test9", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc03db
Push register0, 2
StrictEquals
If loc03e1
Push register0, 3
StrictEquals
If loc03e1
Jump loc03e7
loc03db:Push "place1"
Trace
loc03e1:Push "place3"
Trace
loc03e7:Push "after switch"
Trace
}
DefineFunction2 "test10", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0438
Push register0, 2
StrictEquals
If loc044e
Jump loc0443
loc0438:Push "place1"
Trace
Jump loc0454
loc0443:Push "place2"
Trace
Jump loc0454
loc044e:Push "place3"
Trace
loc0454:Push "after switch"
Trace
}
DefineFunction2 "test11", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc04b5
Push register0, 2
StrictEquals
If loc04c0
Push register0, 3
StrictEquals
If loc04cb
Jump loc04d1
loc04b5:Push "place1"
Trace
Jump loc04d1
loc04c0:Push "place2"
Trace
Jump loc04d1
loc04cb:Push "place3"
Trace
loc04d1:Push "after switch"
Trace
}
DefineFunction2 "test12", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0532
Push register0, 2
StrictEquals
If loc053d
Push register0, 3
StrictEquals
If loc053d
Jump loc0543
loc0532:Push "place1"
Trace
Jump loc0543
loc053d:Push "place3"
Trace
loc0543:Push "after switch"
Trace
}
DefineFunction2 "test13", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0594
Push register0, 2
StrictEquals
If loc059f
Jump loc0594
loc0594:Push "place2"
Trace
Jump loc05a5
loc059f:Push "place3"
Trace
loc05a5:Push "after switch"
Trace
}
DefineFunction2 "test14", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0606
Push register0, 2
StrictEquals
If loc0606
Push register0, 3
StrictEquals
If loc0611
Jump loc0617
loc0606:Push "place2"
Trace
Jump loc0617
loc0611:Push "place3"
Trace
loc0617:Push "after switch"
Trace
}
DefineFunction2 "test15", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0678
Push register0, 2
StrictEquals
If loc067e
Push register0, 3
StrictEquals
If loc0684
Jump loc068a
loc0678:Push "place1"
Trace
loc067e:Push "place2"
Trace
loc0684:Push "place3"
Trace
loc068a:Push "after switch"
Trace
}
DefineFunction2 "test16", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc06eb
Push register0, 2
StrictEquals
If loc06eb
Push register0, 3
StrictEquals
If loc06eb
Jump loc06f1
loc06eb:Push "place3"
Trace
loc06f1:Push "after switch"
Trace
}
DefineFunction2 "test17", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0742
Push register0, 2
StrictEquals
If loc074d
Jump loc0753
loc0742:Push "place1"
Trace
Jump loc0753
loc074d:Push "place2"
Trace
loc0753:Push "after switch"
Trace
}
DefineFunction2 "test18", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc07a4
Push register0, 2
StrictEquals
If loc07aa
Jump loc07b0
loc07a4:Push "place1"
Trace
loc07aa:Push "place2"
Trace
loc07b0:Push "after switch"
Trace
}
DefineFunction2 "test19", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0801
Push register0, 2
StrictEquals
If loc0801
Jump loc0807
loc0801:Push "place2"
Trace
loc0807:Push "after switch"
Trace
}
DefineFunction2 "test20", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0868
Push register0, 2
StrictEquals
If loc0873
Push register0, 3
StrictEquals
If loc0879
Jump loc087f
loc0868:Push "place1"
Trace
Jump loc087f
loc0873:Push "place2"
Trace
loc0879:Push "place3"
Trace
loc087f:Push "after switch"
Trace
}
DefineFunction2 "test21", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc08e0
Push register0, 2
StrictEquals
If loc08e6
Push register0, 3
StrictEquals
If loc08f1
Jump loc08f7
loc08e0:Push "place1"
Trace
loc08e6:Push "place2"
Trace
Jump loc08f7
loc08f1:Push "place3"
Trace
loc08f7:Push "after switch"
Trace
}
DefineFunction2 "test22", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0958
Push register0, 2
StrictEquals
If loc095e
Push register0, 3
StrictEquals
If loc0969
Jump loc096f
loc0958:Push "place1"
Trace
loc095e:Push "place2"
Trace
Jump loc096f
loc0969:Push "place3"
Trace
loc096f:Push "after switch"
Trace
}
DefineFunction2 "test23", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc09c0
Push register0, 2
StrictEquals
If loc09cb
Jump loc09d6
loc09c0:Push "place1"
Trace
Jump loc09dc
loc09cb:Push "place2"
Trace
Jump loc09dc
loc09d6:Push "place3"
Trace
loc09dc:Push "after switch"
Trace
}
DefineFunction2 "test24", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0a2d
Push register0, 2
StrictEquals
If loc0a2d
Jump loc0a33
loc0a2d:Push "place2"
Trace
loc0a33:Push "place3"
Trace
Push "after switch"
Trace
}
DefineFunction2 "test25", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0a9a
Push register0, 2
StrictEquals
If loc0aa0
Push register0, 3
StrictEquals
If loc0aa0
Jump loc0aa6
loc0a9a:Push "place1"
Trace
loc0aa0:Push "place3"
Trace
loc0aa6:Push "after switch"
Trace
}
DefineFunction2 "test26", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0b07
Push register0, 2
StrictEquals
If loc0b07
Push register0, 3
StrictEquals
If loc0b0d
Jump loc0b13
loc0b07:Push "place2"
Trace
loc0b0d:Push "place3"
Trace
loc0b13:Push "after switch"
Trace
}
DefineFunction2 "test27", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0b74
Push register0, 2
StrictEquals
If loc0b7f
Push register0, 3
StrictEquals
If loc0b8a
Jump loc0b90
loc0b74:Push "place1"
Trace
Jump loc0b90
loc0b7f:Push "place2"
Trace
Jump loc0b90
loc0b8a:Push "place3"
Trace
loc0b90:Push "after switch"
Trace
}
DefineFunction2 "test28", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0be1
Push register0, 2
StrictEquals
If loc0bec
Jump loc0bec
loc0be1:Push "place1"
Trace
Jump loc0bf2
loc0bec:Push "place3"
Trace
loc0bf2:Push "after switch"
Trace
}
DefineFunction2 "test29", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0c43
Push register0, 2
StrictEquals
If loc0c43
Jump loc0c4e
loc0c43:Push "place2"
Trace
Jump loc0c54
loc0c4e:Push "place3"
Trace
loc0c54:Push "after switch"
Trace
}
DefineFunction2 "test30", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0cb5
Push register0, 2
StrictEquals
If loc0cc0
Push register0, 3
StrictEquals
If loc0cc0
Jump loc0cc6
loc0cb5:Push "place1"
Trace
Jump loc0cc6
loc0cc0:Push "place3"
Trace
loc0cc6:Push "after switch"
Trace
}
DefineFunction2 "test31", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0d27
Push register0, 2
StrictEquals
If loc0d27
Push register0, 3
StrictEquals
If loc0d32
Jump loc0d38
loc0d27:Push "place2"
Trace
Jump loc0d38
loc0d32:Push "place3"
Trace
loc0d38:Push "after switch"
Trace
}
DefineFunction2 "test32", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0d89
Push register0, 2
StrictEquals
If loc0d89
Jump loc0d89
loc0d89:Push "place3"
Trace
Push "after switch"
Trace
}
DefineFunction2 "test33", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0de6
Push register0, 2
StrictEquals
If loc0dec
Jump loc0de0
loc0de0:Push "place1"
Trace
loc0de6:Push "place2"
Trace
loc0dec:Push "place3"
Trace
Push "after switch"
Trace
}
DefineFunction2 "test34", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0e53
Push register0, 2
StrictEquals
If loc0e59
Push register0, 3
StrictEquals
If loc0e5f
Jump loc0e65
loc0e53:Push "place1"
Trace
loc0e59:Push "place2"
Trace
loc0e5f:Push "place3"
Trace
loc0e65:Push "after switch"
Trace
}
DefineFunction2 "test35", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0ec1
Push register0, 2
StrictEquals
If loc0ec7
Jump loc0eb6
loc0eb6:Push "place1"
Trace
Jump loc0ecd
loc0ec1:Push "place2"
Trace
loc0ec7:Push "place3"
Trace
loc0ecd:Push "after switch"
Trace
}
DefineFunction2 "test36", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0f24
Push register0, 2
StrictEquals
If loc0f2f
Jump loc0f1e
loc0f1e:Push "place1"
Trace
loc0f24:Push "place2"
Trace
Jump loc0f35
loc0f2f:Push "place3"
Trace
loc0f35:Push "after switch"
Trace
}
DefineFunction2 "test37", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc0f96
Push register0, 2
StrictEquals
If loc0fa1
Push register0, 3
StrictEquals
If loc0fa7
Jump loc0fad
loc0f96:Push "place1"
Trace
Jump loc0fad
loc0fa1:Push "place2"
Trace
loc0fa7:Push "place3"
Trace
loc0fad:Push "after switch"
Trace
}
DefineFunction2 "test38", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc1004
Push register0, 2
StrictEquals
If loc1004
Jump loc0ffe
loc0ffe:Push "place1"
Trace
loc1004:Push "place3"
Trace
Push "after switch"
Trace
}
DefineFunction2 "test39", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc106b
Push register0, 2
StrictEquals
If loc1071
Push register0, 3
StrictEquals
If loc107c
Jump loc1082
loc106b:Push "place1"
Trace
loc1071:Push "place2"
Trace
Jump loc1082
loc107c:Push "place3"
Trace
loc1082:Push "after switch"
Trace
}
DefineFunction2 "test40", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc10e3
Push register0, 2
StrictEquals
If loc10ee
Push register0, 3
StrictEquals
If loc10f9
Jump loc10ff
loc10e3:Push "place1"
Trace
Jump loc10ff
loc10ee:Push "place2"
Trace
Jump loc10ff
loc10f9:Push "place3"
Trace
loc10ff:Push "after switch"
Trace
}
DefineFunction2 "test41", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc1160
Push register0, 2
StrictEquals
If loc1166
Push register0, 3
StrictEquals
If loc1166
Jump loc116c
loc1160:Push "place1"
Trace
loc1166:Push "place3"
Trace
loc116c:Push "after switch"
Trace
}
DefineFunction2 "test42", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc11cd
Push register0, 2
StrictEquals
If loc11cd
Push register0, 3
StrictEquals
If loc11d3
Jump loc11d9
loc11cd:Push "place2"
Trace
loc11d3:Push "place3"
Trace
loc11d9:Push "after switch"
Trace
}
DefineFunction2 "test43", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc123a
Push register0, 2
StrictEquals
If loc1245
Push register0, 3
StrictEquals
If loc1245
Jump loc124b
loc123a:Push "place1"
Trace
Jump loc124b
loc1245:Push "place3"
Trace
loc124b:Push "after switch"
Trace
}
DefineFunction2 "test44", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc12ac
Push register0, 2
StrictEquals
If loc12ac
Push register0, 3
StrictEquals
If loc12b7
Jump loc12bd
loc12ac:Push "place2"
Trace
Jump loc12bd
loc12b7:Push "place3"
Trace
loc12bd:Push "after switch"
Trace
}
DefineFunction2 "test45", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc131e
Push register0, 2
StrictEquals
If loc131e
Push register0, 3
StrictEquals
If loc131e
Jump loc1324
loc131e:Push "place3"
Trace
loc1324:Push "after switch"
Trace
}
DefineFunction2 "test46", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc1375
Push register0, 2
StrictEquals
If loc1381
Jump loc137b
loc1375:Push "place1"
Trace
loc137b:Push "place2"
Trace
loc1381:Push "place3"
Trace
Push "after switch"
Trace
}
DefineFunction2 "test47", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc13e8
Push register0, 2
StrictEquals
If loc13ee
Push register0, 3
StrictEquals
If loc13f4
Jump loc13fa
loc13e8:Push "place1"
Trace
loc13ee:Push "place2"
Trace
loc13f4:Push "place3"
Trace
loc13fa:Push "after switch"
Trace
}
DefineFunction2 "test48", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc144b
Push register0, 2
StrictEquals
If loc145c
Jump loc1456
loc144b:Push "place1"
Trace
Jump loc1462
loc1456:Push "place2"
Trace
loc145c:Push "place3"
Trace
loc1462:Push "after switch"
Trace
}
DefineFunction2 "test49", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc14b3
Push register0, 2
StrictEquals
If loc14c4
Jump loc14b9
loc14b3:Push "place1"
Trace
loc14b9:Push "place2"
Trace
Jump loc14ca
loc14c4:Push "place3"
Trace
loc14ca:Push "after switch"
Trace
}
DefineFunction2 "test50", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc152b
Push register0, 2
StrictEquals
If loc1536
Push register0, 3
StrictEquals
If loc153c
Jump loc1542
loc152b:Push "place1"
Trace
Jump loc1542
loc1536:Push "place2"
Trace
loc153c:Push "place3"
Trace
loc1542:Push "after switch"
Trace
}
DefineFunction2 "test51", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc1593
Push register0, 2
StrictEquals
If loc1599
Jump loc1593
loc1593:Push "place2"
Trace
loc1599:Push "place3"
Trace
Push "after switch"
Trace
}
DefineFunction2 "test52", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc1600
Push register0, 2
StrictEquals
If loc1606
Push register0, 3
StrictEquals
If loc1611
Jump loc1617
loc1600:Push "place1"
Trace
loc1606:Push "place2"
Trace
Jump loc1617
loc1611:Push "place3"
Trace
loc1617:Push "after switch"
Trace
}
DefineFunction2 "test53", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc1678
Push register0, 2
StrictEquals
If loc167e
Push register0, 3
StrictEquals
If loc167e
Jump loc1684
loc1678:Push "place1"
Trace
loc167e:Push "place3"
Trace
loc1684:Push "after switch"
Trace
}
DefineFunction2 "test54", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc16e0
Push register0, 2
StrictEquals
If loc16e0
Jump loc16d5
loc16d5:Push "place1"
Trace
Jump loc16e6
loc16e0:Push "place3"
Trace
loc16e6:Push "after switch"
Trace
}
DefineFunction2 "test55", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc1747
Push register0, 2
StrictEquals
If loc1747
Push register0, 3
StrictEquals
If loc174d
Jump loc1753
loc1747:Push "place2"
Trace
loc174d:Push "place3"
Trace
loc1753:Push "after switch"
Trace
}
DefineFunction2 "test56", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc17af
Push register0, 2
StrictEquals
If loc17ba
Jump loc17a4
loc17a4:Push "place1"
Trace
Jump loc17c0
loc17af:Push "place2"
Trace
Jump loc17c0
loc17ba:Push "place3"
Trace
loc17c0:Push "after switch"
Trace
}
DefineFunction2 "test57", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc1821
Push register0, 2
StrictEquals
If loc182c
Push register0, 3
StrictEquals
If loc1837
Jump loc183d
loc1821:Push "place1"
Trace
Jump loc183d
loc182c:Push "place2"
Trace
Jump loc183d
loc1837:Push "place3"
Trace
loc183d:Push "after switch"
Trace
}
DefineFunction2 "test58", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc189e
Push register0, 2
StrictEquals
If loc18a9
Push register0, 3
StrictEquals
If loc18a9
Jump loc18af
loc189e:Push "place1"
Trace
Jump loc18af
loc18a9:Push "place3"
Trace
loc18af:Push "after switch"
Trace
}
DefineFunction2 "test59", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc1910
Push register0, 2
StrictEquals
If loc1910
Push register0, 3
StrictEquals
If loc191b
Jump loc1921
loc1910:Push "place2"
Trace
Jump loc1921
loc191b:Push "place3"
Trace
loc1921:Push "after switch"
Trace
}
DefineFunction2 "test60", 0, 2, false, false, true, false, true, false, true, false, false {
Push 100
RandomNumber
StoreRegister 1
Pop
Push register1
StoreRegister 0
Push 1
StrictEquals
If loc1982
Push register0, 2
StrictEquals
If loc1982
Push register0, 3
StrictEquals
If loc1982
Jump loc1988
loc1982:Push "place3"
Trace
loc1988:Push "after switch"
Trace
}
Push "switchVariantsTest"
Trace
