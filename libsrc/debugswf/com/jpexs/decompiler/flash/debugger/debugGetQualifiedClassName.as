package com.jpexs.decompiler.flash.debugger {		
	
	import flash.utils.getQualifiedClassName;
	
	public function debugGetQualifiedClassName(value:*):String {
		var r:String = getQualifiedClassName(value);
		switch(r){
			case "com.jpexs.decompiler.flash.debugger.DebugLoader":
				return "flash.display.Loader";	
		}
		return r;
	}	
	
}
