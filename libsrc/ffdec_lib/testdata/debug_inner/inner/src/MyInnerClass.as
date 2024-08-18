package 
{
	import flash.display.Loader;
	import flash.display.LoaderInfo;
	import flash.display.Sprite;
	import flash.events.Event;
	import flash.text.TextField;
	import flash.text.TextFormat;
	import flash.utils.ByteArray;
	
	public class MyInnerClass 
	{
		[Embed(source="../../inner2.swf", mimeType="application/octet-stream")] 
        public var binaryData2Class:Class;	
		
		private var root:Sprite;
		
		public function MyInnerClass() 
		{			
		}
		
		public function run(s:Sprite): void {
			var myvar:int = 1;
			trace("hello from inner class 1");
			
			var textField:TextField = new TextField();

			textField.text = "Hello from inner1 !";

			var textFormat:TextFormat = new TextFormat();
			textFormat.size = 24;
			textFormat.color = 0x000000;
			textField.setTextFormat(textFormat);
						
			textField.width = 200;
			
			s.addChild(textField);

			textField.x = 50;
			textField.y = 50;
			
			root = s;
			var byteArray:ByteArray = new binaryData2Class() as ByteArray;
			var loader:Loader = new Loader();
			loader.contentLoaderInfo.addEventListener(Event.COMPLETE, onLoaderComplete);
			loader.loadBytes(byteArray);
		}
		
		private function onLoaderComplete(event:Event):void {
			var loaderInfo:LoaderInfo = event.target as LoaderInfo;
			var className:String = "MyInnerClass2";
			var LoadedClass:Class = loaderInfo.applicationDomain.getDefinition(className) as Class;
			var instance:* = new LoadedClass();
			instance.run(root);
		}		
		
	}

}