package tests_uses
{
	
	public class TestOtherClass
	{
		public function methodBody(): void {
            var tc:TestClass = new TestClass();
            trace("method");
        }
        
        public function argsMethod(tc:TestClass): void {
            trace("argsMethod");
        }
        
        public function returnTypeMethod(): TestClass {
            trace("returnTypeMethod");
            return null;
        }
        
        public function methodCall(): void {
            var tc:TestClass = new TestClass();
            tc.classMethod();
        }
        
        public function methodCall2(): void {
            var tc2:TestClass2 = new TestClass2();
            tc2.classMethod();
        }
        
        public function varUse(): void {
            var tc:TestClass = new TestClass();
            trace(tc.parentVar);
            trace(tc.classVar);
            
        }
	}
}
