package
{
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
			var t:Test = new Test();
			t.testHello();
		}
		
		public static var staticVariable:int = 5;
		public static var staticVariable2:int = 5;
		
		public function testStatic2():int
		{
			return TestMovie.staticVariable + TestMovie.staticVariable2;
		}
		
		public function testStatic():void
		{
			var testFunction:Function = function(name:String):*
			{
				return TestMovie[name];
			};
			trace(testFunction("staticVariable"));
		}
	}
}