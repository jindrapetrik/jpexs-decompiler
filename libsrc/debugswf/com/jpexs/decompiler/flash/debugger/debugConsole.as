package com.jpexs.decompiler.flash.debugger {
	
	import flash.external.ExternalInterface;
	
	public function debugConsole(msg):*{
		if(ExternalInterface.available)
		  ExternalInterface.call("console.log",""+msg);
		return msg;
	}
	
}
