package com.jpexs.decompiler.flash.debugger {
	
	import flash.utils.getQualifiedClassName;
	import flash.utils.getQualifiedSuperclassName;
	
	
	public function debugGetQualifiedSuperclassName(value:*):String {
		var thisClass:String = getQualifiedClassName(value);
		var superClass:String = getQualifiedSuperclassName(value);
		switch(thisClass){
			case "com.jpexs.decompiler.flash.debugger.DebugLoader":
				return "flash.display.DisplayObjectContainer";	
		}
		return superClass;
	}		
	
}
