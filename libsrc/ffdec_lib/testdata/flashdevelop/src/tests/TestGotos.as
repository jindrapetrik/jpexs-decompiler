package tests
{
	
	public class TestGotos
	{
		
		public final function run(param1:Object):int
		{
			var a:Boolean = true;
			var b:Boolean = false;
			
			if (a)
			{
				trace("A");
			}
			else if (b)
			{
				trace("B");
				
			}			
			else
			{
				try
				{
					if (a)
					{
						return 7;
					}
					trace("x");
				}
				catch (e:Error)
				{
					trace("z");
				}
				trace("after");
			}
			
			return 89;
		}
	
	}

}