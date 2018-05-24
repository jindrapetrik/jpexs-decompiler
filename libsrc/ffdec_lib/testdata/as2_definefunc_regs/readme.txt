This is Flash MX Professional 2004 project with one class and export settings set to Flash 6.

The swf file has unusual structure:
From Flash 7 onwards, functions are stored as ActionDefineFunction2 with use up to 255 local
registers. When exporting SWF to lower formats Flash5-Flash6 in Flash IDE (for example MX 2004), all ActionDefineFunction2 are replaced with ActionDefineFunction, and it also use local registers (4 of them are
available). The code of ActionDefineFunction is also modified that it pushes all previous values of registers on the code start and pops them back on code exit or when return action shows up. All returns are replaced with jump to popping part. This makes code flow tangled and the decompiler must do some special treatment.