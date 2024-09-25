package tests
{
	
	public class TestOptimizationWhile
	{
		public function run():*
		{
            // Add more than 3 variables. 
            // Optimization happens from register 4 on.
            // (setlocal X takes more bytes than dup)
			var a:int = 1;
            var b:int = 2;
            var c:int = 3;
            
            var d:int = 4;
            
            while(true)
             {
                d = Math.round(Math.random() * 10);
                if(d >= 10)
                {
                   break;
                }
                trace("xxx");
                d++;
             } 
		}
	}
}
