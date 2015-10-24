package com.jpexs.decompiler.flash.debugger {
	
	import flash.external.ExternalInterface;
	
	public function debugConsole(...msg):*{
		for each(var n in msg){
			if(ExternalInterface.available)
		  		ExternalInterface.call("console.log",""+n);
		}
	}
	
}
