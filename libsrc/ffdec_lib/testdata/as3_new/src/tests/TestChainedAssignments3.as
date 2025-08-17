package tests
{
	
	public class TestChainedAssignments3
	{
        private var prop:int;
    
		public function run():*
		{
			var a:int = 0;
			var b:int = 0;
            prop = a = b = 4;
            if (a == 2) {
                trace("OK: " + a);                
            }
		}
	}
}
