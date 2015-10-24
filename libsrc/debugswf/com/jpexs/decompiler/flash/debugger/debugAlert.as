package com.jpexs.decompiler.flash.debugger {
	
	import flash.external.ExternalInterface;
	
	public function debugAlert(...msg):void{
		for each(var n in msg){
			if(ExternalInterface.available)
		  	ExternalInterface.call("alert",""+n);
		}
	}
	
}
