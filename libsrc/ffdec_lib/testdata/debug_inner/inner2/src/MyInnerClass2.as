package 
{
	import flash.display.Sprite;
	import flash.text.TextField;
	import flash.text.TextFormat;
	
	public class MyInnerClass2 
	{
		
		public function MyInnerClass2() 
		{
			
		}
		
		public function run(s:Sprite): void {
			var myvar:int = 2;
			trace("hello from inner class 2");
			
			var textField:TextField = new TextField();

			textField.text = "Hello from inner2 !";

			var textFormat:TextFormat = new TextFormat();
			textFormat.size = 24;
			textFormat.color = 0x000000;
			textField.setTextFormat(textFormat);
			textField.width = 200;
						
			s.addChild(textField);

			textField.x = 50;
			textField.y = 75;
			
		}
	}

}