package com.jpexs.decompiler.flash.debugger {
	
	import flash.display.MovieClip;
	
	
	public class DebugMain extends MovieClip {
		
		
		public function DebugMain() {
			debugAlert("test alert");
			debugConsole("test console");			
			debugSocket("test proxy");
			debugTrace("test trace");
			var loader:DebugLoader = new DebugLoader();
			var ldr = debugGetDefinitionByName("flash.display.Loader");
			debugDescribeType(ldr);
			var name = debugGetQualifiedClassName(ldr);
			var par = debugGetQualifiedSuperclassName(ldr);
		}
	}		
	
}
