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