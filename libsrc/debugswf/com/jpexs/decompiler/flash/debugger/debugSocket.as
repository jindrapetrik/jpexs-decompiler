package com.jpexs.decompiler.flash.debugger {
	import flash.system.Capabilities;
	
	public function debugSocket(...msg):*{		
		for each(var n in msg){
			//only on webpages or activex
			if(Capabilities.playerType == 'PlugIn'
			   || Capabilities.playerType == 'ActiveX'){
				DebugConnection.writeMsg(n);		
			}
		}
	}
	
}
