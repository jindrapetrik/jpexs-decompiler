package tests
{
	
	public class TestAndInt
	{
		public function run():*
		{        
            var a:int = 1;
            var b:int = 5;
            if (0 && (1 || a < b))
            {
            	trace("okay");
            }               
		}
	}
}
