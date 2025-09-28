package tests
{
	
	public class TestChainedAssignments1
	{
        public function run():*
		{
            trace("c = b = a = 5;");
			var a:int = 0;
			var b:int = 0;
			var c:int = 0;            
			c = b = a = 5;			
		}	
	}
}
