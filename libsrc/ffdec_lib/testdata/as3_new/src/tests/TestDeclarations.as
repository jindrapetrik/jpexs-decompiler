package tests
{
	
	public class TestDeclarations
	{
		public function run():*
		{
			var vall:* = undefined;
			var vstr:String = null;
			var vint:int = 0;
			var vuint:uint = 0;
			var vclass:TestClass1 = null;
			var vnumber:Number = NaN;
			var vobject:Object = null;
			vall = 6;
			vstr = "hello";
			vuint = 7;
			vint = -4;
			vclass = new TestClass1();
			vnumber = 0.5;
			vnumber = 6;
			vobject = vclass;
		}
	}
}

class TestClass1
{

}