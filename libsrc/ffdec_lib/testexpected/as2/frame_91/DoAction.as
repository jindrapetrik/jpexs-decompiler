ConstantPool "dynamicGetUrlTest", "n", "", "_level", "print:#bmax", "printasbitmap:#bmax", "something.swf", "v", "_root", "something", "r", "file", ".swf", "_blank", "FSCommand:", "test", "xx"
Push "dynamicGetUrlTest"
Trace
Push "n", 5
DefineLocal
Push "", "_level", "n"
GetVariable
StringAdd
GetURL2 false, false, 0
Push "print:#bmax", "_level", "n"
GetVariable
StringAdd
GetURL2 false, false, 0
Push "printasbitmap:#bmax", "_level", "n"
GetVariable
StringAdd
GetURL2 false, false, 0
Push "something.swf", "_level", "n"
GetVariable
StringAdd
GetURL2 false, false, 2
Push "v", "_root"
GetVariable
Push "something"
GetMember
DefineLocal
Push "print:#bmax", "v"
GetVariable
GetURL2 false, false, 0
Push "printasbitmap:#bmax", "v"
GetVariable
GetURL2 false, false, 0
Push "r", 5
DefineLocal
Push "file", "r"
GetVariable
Add2
Push ".swf"
Add2
Push "_blank"
GetURL2 false, false, 2
Push "FSCommand:", "test", "r"
GetVariable
Add2
StringAdd
Push "xx"
GetURL2 false, false, 0
