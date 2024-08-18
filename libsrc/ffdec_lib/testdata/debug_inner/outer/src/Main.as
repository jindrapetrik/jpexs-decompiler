package
{
	import flash.display.Sprite;
	import flash.events.Event;
	import flash.display.Loader;
	import flash.display.LoaderInfo;
	import flash.utils.ByteArray;
	
	/**
	 * ...
	 * @author Jindra
	 */
	public class Main extends Sprite 
	{
		[Embed(source="../../inner.swf", mimeType="application/octet-stream")] 
        public var binaryDataClass:Class;
        
        [Embed(source="../image.png", mimeType="application/octet-stream")] 
        public var imageDataClass:Class;		
		
		public function Main() 
		{
			if (stage) init();
			else addEventListener(Event.ADDED_TO_STAGE, init);
		}
		
		private function init(e:Event = null):void 
		{
			removeEventListener(Event.ADDED_TO_STAGE, init);
			
			var byteArray:ByteArray = new binaryDataClass() as ByteArray;
			var loader:Loader = new Loader();
			loader.contentLoaderInfo.addEventListener(Event.COMPLETE, onLoaderComplete);
			loader.loadBytes(byteArray);
            
            var imageByteArray:ByteArray = new imageDataClass() as ByteArray;
            var loader2:Loader = new Loader();
			loader2.contentLoaderInfo.addEventListener(Event.COMPLETE, onImageLoaderComplete);
			loader2.loadBytes(imageByteArray);
		}
		        
        private function onImageLoaderComplete(event:Event):void {
            var loader:Loader = LoaderInfo(event.currentTarget).loader;
            addChild(loader);
        }
		private function onLoaderComplete(event:Event):void {
			var loaderInfo:LoaderInfo = event.target as LoaderInfo;
			var className:String = "MyInnerClass";
			var LoadedClass:Class = loaderInfo.applicationDomain.getDefinition(className) as Class;
			var instance:* = new LoadedClass();
			instance.run(this);
		}
		
	}
	
}
