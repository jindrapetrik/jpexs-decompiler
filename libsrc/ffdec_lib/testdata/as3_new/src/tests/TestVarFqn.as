package tests
{
    import tests_classes.mypackage1.TestClass;
    import tests_classes.mypackage2.TestClass;
		    
	public class TestVarFqn
	{
        private var c1:tests_classes.mypackage1.TestClass;
        private var c2:tests_classes.mypackage2.TestClass;
    
		public function run(TestClass:int):*
		{
            var b:int = TestClass + 5; //TestClass here is variable name (body trait name), should not be displayed as FQN
            var f:Function = function(x:int, y:int):int { //This triggers body traits generation
                return x + y + TestClass;
            };
		}
	}
}
