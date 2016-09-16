package  {
	import flash.display.Sprite;
	import flash.text.TextField;
	import flash.utils.ByteArray;

	public class EmptyDocument extends Sprite{

		public var attr:int = 5;		

		public function EmptyDocument() {
			var display_txt:TextField = new TextField();
			display_txt.text = "Hello World!";
			display_txt.width = 300;
			addChild(display_txt);						
			var mc = new MyClass();
		}				
	}
	
}
