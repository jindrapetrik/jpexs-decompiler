Push "withTest"
Trace
Push "before"
Trace
Push "_root"
GetVariable
Push "something"
GetMember
With {
Push "somesub", 5
SetVariable
Push "subvar"
GetVariable
With {
Push "somesub2", 4
SetVariable
}
Push "after1"
Trace
}
Push "after"
Trace
