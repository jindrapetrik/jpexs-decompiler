package tests
{
	
	public class TestIfInIf
	{
		
		public function run() : int
		{
			var k:int = 5;
			if (k > 5 && k <20)
			{
				trace("A");
				
				if (k < 4)
				{
					return 1;
				}
			}
			else if (k > 4 && k<10)
			{
				trace("B");
				if (k < 7)
				{
					return 2;
				}
			}
			
			trace("C");
			return 7;
		}
		
	}

}