package com.jpexs.decompiler.flash.debugger {
	
	import flash.display.Loader;
	import flash.net.URLRequest;
	import flash.system.LoaderContext;
	import flash.utils.ByteArray;
    import flash.net.URLLoader;
    import flash.net.URLLoaderDataFormat;
    import flash.net.URLRequest;
    import flash.events.Event;
	
	public class DebugLoader extends Loader {	
    
        private var lastLoadedContext:LoaderContext = null;
        private var urlLoader:URLLoader = null;
        private var lastLoadedRequest:URLRequest = null;
        private var lastModifiedByteArray:ByteArray = null;
		
		public override function load(request:URLRequest, context:LoaderContext = null):void {
            lastLoadedRequest = request;			
            lastLoadedContext = context;
            
            urlLoader = new URLLoader();
            urlLoader.dataFormat = URLLoaderDataFormat.BINARY;
            urlLoader.addEventListener(Event.COMPLETE, onURLLoaderComplete);
            urlLoader.load(request);
		}
        
        private function onURLLoaderComplete(event:Event):void {
            var dataBytes:ByteArray = urlLoader.data as ByteArray;
            loadBytesInternal(dataBytes, lastLoadedContext, lastLoadedRequest.url);
        }
		
        private function loadBytesInternal(bytes:ByteArray, context:LoaderContext = null, url:String = "") {
            lastModifiedByteArray = new ByteArray();
            lastLoadedContext = context;        
            DebugConnection.modifyLoaderBytesWithUrl(bytes, lastModifiedByteArray, url, onModifiedDataLoaded);			
        }
        
        private function onModifiedDataLoaded() {            
            super.loadBytes(lastModifiedByteArray, lastLoadedContext);
        }
        
		public override function loadBytes(bytes:ByteArray, context:LoaderContext = null):void {
			loadBytesInternal(bytes, context);
		}
		
		public override function toString():String {
			return "[object Loader]";
		}
	}
	
}
