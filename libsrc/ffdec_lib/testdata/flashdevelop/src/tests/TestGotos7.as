package tests
{
	
	public class TestGotos7
	{
		
		public function run():void
		{
			
			for (var i:int = 0; i < 10; i++)
			{
				switch (i)
				{
				case 0: 
					trace("zero");
					continue;
				case 5: 
					trace("five");
					break;
				case 10: 
					trace("ten");
					break;
				case 1: 
					if (i == 7)
					{
						continue;
					}
					trace("one");
				default: 
					trace("def");
				}
				trace("before loop end");
			}
		}
	
	}

}