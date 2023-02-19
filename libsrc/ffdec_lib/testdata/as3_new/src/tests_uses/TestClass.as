package tests_uses
{
	
	public class TestClass extends TestParentClass implements TestInterface
	{
        public var classVar:int = 2;
    
		public function interfaceMethod(): void {
            trace("interfaceMethod");
        }
        
        public function parentInterfaceMethod(): void {
            trace("parentInterfaceMethod");
        }
        
        public function classMethod(): void {
            trace("classMethod");
        }
	}
}
