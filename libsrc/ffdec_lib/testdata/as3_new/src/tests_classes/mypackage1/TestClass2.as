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
            var res:String = a.testCall() +  
                             b.testCall() +  
                             c.testCall() + 
                             public::testCall2() + 
                             private::testCall3() + 
                             protected::testCall4() + 
                             protected::testCall5() +
                             internal::testCall6() +
                             myNamespace::testCall3();
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
        
        myNamespace function testCall4() : String
		{
			return myNamespace::testCall3();
		}
        
        myNamespace static function testCall5() : String
		{
			return "x";
		}
        
        myNamespace function testCall6() : String
		{
			return "y";
		}

		public function testCall2() : String
		{
			return "2";
		}
        
        private function testCall3() : String
        {
            return "3";        
        }
        
        protected function testCall4() : String
        {
            return "4";        
        }
        
        static protected function testCall5(): String
        {
            return "5";
        }
        
        internal function testCall6(): String
        {
            return "6";
        }
	}
}