package tests 
{
	public class TestGotos3 
	{
		
		public function run() : void
		{
			var a:int = 5;
			if (a > 5)
			{
				for (var i:int = 0; i < 5; i++)
				{
					if (i > 3)
					{
						trace("A");
						if (i == 4)
						{
							break;
						}
					}
					trace("B");
				}
			}
			else
			{
				trace("C");
			}
			trace("return");
		}
		
	}

}