package pkg_as3 {
	
	import flash.display.MovieClip;
	import flash.display.Loader;
	import flash.net.URLRequest;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	
	
	public class Main extends MovieClip {
		var loader:Loader;
		
		public function Main()
        {
            if (stage) init();
            else addEventListener(Event.ADDED_TO_STAGE, init);
        }
        
        private function init(e:Event = null):void
        {
            removeEventListener(Event.ADDED_TO_STAGE, init);
            
			loader = new Loader();
			
			var url:String = "external.swf";
			var urlRequest:URLRequest = new URLRequest(url);
			loader.contentLoaderInfo.addEventListener(Event.COMPLETE, onLoadComplete);			
			loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, onIOError);			
			loader.load(urlRequest);
        }  
		
		function onLoadComplete(event:Event):void {
			addChild(loader);			
			trace("Movie added");
		}
		
		function onIOError(event:IOErrorEvent):void {
			trace("Error loading SWF: " + event.text);
		}
	}
	
}
