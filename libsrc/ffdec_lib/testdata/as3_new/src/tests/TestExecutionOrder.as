package tests
{
	
	public class TestExecutionOrder
	{
		public function run():*
		{
			var m:MyClass;
			m.x = (m = create() as MyClass).x + 5;
            trace(m.x);
		}
        
        
        private static function create(): Object {
            return new MyClass();
        }
	}	
}

class MyClass {
	public var x:int = 1;
	public var y:int = 1;
}