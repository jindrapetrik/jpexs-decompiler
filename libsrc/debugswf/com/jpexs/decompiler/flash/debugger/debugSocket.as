package com.jpexs.decompiler.flash.debugger {
	import flash.system.Capabilities;
	
	public function debugSocket(msg):*{
		//only on webpages or activex
		if(Capabilities.playerType == 'PlugIn'
		   || Capabilities.playerType == 'ActiveX'){
			DebugConnection.writeMsg(msg);		
		}
		return msg;
	}
	
}
