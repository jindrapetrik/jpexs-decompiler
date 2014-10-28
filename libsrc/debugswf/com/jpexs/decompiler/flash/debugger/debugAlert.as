package com.jpexs.decompiler.flash.debugger {
	
	import flash.external.ExternalInterface;
	
	public function debugAlert(msg):*{
		if(ExternalInterface.available)
		  ExternalInterface.call("alert",""+msg);
		return msg;		
	}
	
}
