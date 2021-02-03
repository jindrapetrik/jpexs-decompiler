package tests_classes.mypackage1
{
	import tests_classes.mypackage2.TestClass;
	import tests_classes.mypackage3.TestClass;

	public class TestClass2
	{
		public function testCall() : String
		{
			var a : tests_classes.mypackage1.TestClass;
			a = new tests_classes.mypackage1.TestClass();
			var b : tests_classes.mypackage2.TestClass;
			b = new tests_classes.mypackage2.TestClass();
			var c : tests_classes.mypackage3.TestClass;
			c = new tests_classes.mypackage3.TestClass();
			var res:String = a.testCall() +  b.testCall() +  c.testCall() + testCall2() + myNamespace::testCall3();
			trace(res);
			return res;
		}

		myNamespace function testCall2() : String
		{
			return "1";
		}

		myNamespace function testCall3() : String
		{
			return myNamespace::testCall2();
		}

		public function testCall2() : String
		{
			return "2";
		}
	}
}