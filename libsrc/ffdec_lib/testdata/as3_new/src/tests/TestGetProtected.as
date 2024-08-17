package tests
{
	
	public class TestGetProtected
	{
        protected var attr:int = 5;
    
		public function run():*
		{
            var c:InnerClass = new InnerClass();
            c.attr = 2;
			var a:int = attr;
            trace(a);       
		}
	}
}

class InnerClass
{
    public var attr:int = 1;
}
