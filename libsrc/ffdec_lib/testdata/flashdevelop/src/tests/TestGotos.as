package tests
{
	
	public class TestGotos
	{
		
		public final function run(param1:Object):int
		{
			var _loc2_:Boolean = true;
			var _loc3_:Boolean = false;
			var _loc4_:Boolean = false;
			
			if (_loc2_)
			{
				trace("A");
			}
			else if (_loc3_)
			{
				trace("B");
				
			}			
			else
			{
				try
				{
					if (_loc2_)
					{
						return 7;
					}
					trace("x");
				}
				catch (e:Error)
				{
					trace("z");
				}
			}
			
			return 89;
		}
	
	}

}