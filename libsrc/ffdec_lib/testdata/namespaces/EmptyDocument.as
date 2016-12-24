package  {
	import flash.display.Sprite;
	import flash.text.TextField;
	import flash.utils.ByteArray;

	public class EmptyDocument extends Sprite{

		public var attr:int = 5;		

		public function EmptyDocument() {
			var mc:MyClass = new MyClass();						
			var mc2:MyClass2 = new MyClass2()
			var r = mc.getResult() + mc2.getResult();
			
			var expected = 561;
			
			var display_txt:TextField = new TextField();
			display_txt.text = "Actual = "+r+", expected = "+expected+", "+(r==expected?"OKAY":"FAIL");
			display_txt.width = 300;
			addChild(display_txt);						
			
		}				
	}
	
}
