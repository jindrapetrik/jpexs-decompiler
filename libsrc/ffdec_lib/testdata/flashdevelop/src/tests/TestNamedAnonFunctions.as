package tests
{
   public class TestNamedAnonFunctions
   {
      public function run() : *
      {
         var test:* = new function testFunc(param1:*, param2:int, param3:Array):Boolean
			{
				return (param1 as TestClass2).attrib1 == 5;
			};
      }	
   }
}

class TestClass2
{
	public var attrib1:int;
}
	
