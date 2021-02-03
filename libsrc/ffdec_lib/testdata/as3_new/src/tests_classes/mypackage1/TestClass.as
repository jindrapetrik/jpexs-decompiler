package tests_classes.mypackage1
{
	import tests_classes.mypackage2.TestClass;

	public class TestClass implements tests_classes.mypackage1.TestInterface
	{
		public function testCall() : String
		{
			trace("pkg1hello");
			return "pkg1hello";
		}
		
		public function testMethod1() : void {
			var a : tests_classes.mypackage1.TestInterface = this;
			a.testMethod1();
			var b : tests_classes.mypackage2.TestInterface = this;
			b = new tests_classes.mypackage2.TestClass();
		}

		public function testMethod2() : void {
			var a : tests_classes.mypackage1.TestInterface = this;
			a.testMethod1();
			var b : tests_classes.mypackage2.TestInterface = this;
			b = new tests_classes.mypackage2.TestClass();
		}
	}
}