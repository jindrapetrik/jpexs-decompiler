package com.jpexs.decompiler.flash.debugger {
	
	
	public function debugTrace(...msg){
		for each(var n in msg){
			debugConsole(n);
			debugSocket(n);
		}
	}
	
}
