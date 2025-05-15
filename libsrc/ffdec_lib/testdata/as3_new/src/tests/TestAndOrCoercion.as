package tests
{
    import tests_classes.mypackage1.TestClass;
    import tests_classes.mypackage1.TestInterface;
	
    /*
     * This is more like a direct editation test than decompilation.
     * The compiler should add coercion around both sides of AND and OR operators,
     * if there is specific type required
     */
	public class TestAndOrCoercion
	{
        private var ti:TestInterface;
        private var tc:TestClass;
        private var i:int = 5;
        private var j:int = 6;
        private var tx:TestInterface = ti || tc;
    
		public function run():TestInterface
		{
			var x:TestInterface = ti || ((ti = new TestClass()) && (ti = new TestClass()));
            var y:TestInterface = ti && (ti = new TestClass());
            var z:TestClass = tc || (tc = new TestClass());
            
            this.ti = ti && (ti = new TestClass());
            
            var a:* = ti && (ti = new TestClass());
            
            var b:int = 1 + (i || j); //no coercion
            
            test(ti && (ti = new TestClass()));
            
            return ti && (ti = new TestClass());
		}
        
        public function test(p:TestInterface): void
        {
        
        }
	}
}
