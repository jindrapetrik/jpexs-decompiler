package classes.mypackage1
{
	import classes.mypackage2.TestClass;

	public class TestClass implements classes.mypackage1.TestInterface
	{
		public function testCall() : String
		{
			trace("pkg1hello");
			return "pkg1hello";
		}
		
		public function testMethod1() : void {
			var a : classes.mypackage1.TestInterface = this;
			a.testMethod1();
			var b : classes.mypackage2.TestInterface = this;
			b = new classes.mypackage2.TestClass();
		}

		public function testMethod2() : void {
			var a : classes.mypackage1.TestInterface = this;
			a.testMethod1();
			var b : classes.mypackage2.TestInterface = this;
			b = new classes.mypackage2.TestClass();
		}
	}
}