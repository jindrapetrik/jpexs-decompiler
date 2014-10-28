package com.jpexs.decompiler.flash.debugger {
	
	import flash.display.MovieClip;
	
	
	public class DebugMain extends MovieClip {
		
		
		public function DebugMain() {
			debugAlert("test alert");
			debugConsole("test console");			
			debugSocket("test proxy");
			debugTrace("test trace");
		}
	}		
	
}
