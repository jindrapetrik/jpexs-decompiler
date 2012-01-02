package {
	import flash.display.Sprite;
	import flash.text.TextField;
	import classes.Test;
	public class TestMovie extends Sprite
	{

		public function TestMovie()
		{
			var display_txt:TextField = new TextField();
			display_txt.text = "Hello World!";
			addChild(display_txt);
			var t:Test=new Test();
			t.testHello();
		}
	}
}