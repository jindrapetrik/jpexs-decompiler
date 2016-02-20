package com.jpexs.decompiler.flash.debugger {
	
	import flash.display.Loader;
	import flash.net.URLRequest;
	import flash.system.LoaderContext;
	import flash.utils.ByteArray;
	
	public class DebugLoader extends Loader {
		
		
		public override function load(request:URLRequest, context:LoaderContext = null):void {
			DebugConnection.writeLoaderURL(request.url);
			super.load(request,context);
		}
		
		public override function loadBytes(bytes:ByteArray, context:LoaderContext = null):void {
			DebugConnection.writeLoaderBytes(bytes);
			super.loadBytes(bytes,context);
		}
		
		public override function toString():String {
			return "[object Loader]";
		}
	}
	
}
