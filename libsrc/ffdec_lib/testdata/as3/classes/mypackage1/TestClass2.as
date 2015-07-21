package classes.mypackage1
{
	import classes.mypackage2.TestClass;
	import classes.mypackage3.TestClass;

	public class TestClass2
	{
		public function testCall()
		{
			var a : classes.mypackage1.TestClass;
			a = new classes.mypackage1.TestClass();
			var b : classes.mypackage2.TestClass;
			b = new classes.mypackage2.TestClass();
			var c : classes.mypackage3.TestClass;
			c = new classes.mypackage3.TestClass();
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