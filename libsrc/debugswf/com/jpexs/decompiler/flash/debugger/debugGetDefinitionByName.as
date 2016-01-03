package com.jpexs.decompiler.flash.debugger {
	
	
	import flash.utils.getDefinitionByName;
	
	public function debugGetDefinitionByName(name:String):Object {
		switch(name){
			case "flash.display.Loader":
				return getDefinitionByName("com.jpexs.decompiler.flash.debugger.DebugLoader");	
		}
		return getDefinitionByName(name);
	}	
	
}
