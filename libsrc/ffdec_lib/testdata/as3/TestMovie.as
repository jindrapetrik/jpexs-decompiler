package 
{
	import flash.display.Sprite;
	import flash.text.TextField;
	import classes.Test;
	import classes.mypackage1.TestClass2;
	
	public class TestMovie extends Sprite
	{
		public static const instance:TestMovie = new TestMovie();
		public static var k = {a:6,b:7,c:9};
		public function TestMovie()
		{
			var display_txt:TextField = new TextField();
			display_txt.text = "Hello World!";
			display_txt.width = 300;
			addChild(display_txt);
			var t:Test = new Test();
			t.testHello();
			testObj({a:5,b:6,c:7});
			var t2:TestClass2 = new TestClass2();
			display_txt.text = t2.testCall();
		}

		public function testObj(o:Object)
		{
			trace(o);
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