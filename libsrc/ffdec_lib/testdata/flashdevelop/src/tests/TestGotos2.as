package tests 
{
	public class TestGotos2 
	{
		
		public function run() :int
		{
			var a : Boolean = true;
			var b : Boolean = false;
			var c : Boolean = true;
			
			if (a)
			{
				
				if (b)
				{
					trace("A");
					if (c)
					{
						trace("B");
					}      
				}				
			}
			else
			{
				trace("E");
			}
			return 5;

		}
		
	}

}