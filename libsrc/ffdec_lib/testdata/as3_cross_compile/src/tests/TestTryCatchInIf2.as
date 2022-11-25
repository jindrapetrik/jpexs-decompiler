package tests
{
	
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchInIf2
	{
		
		public function run():void
		{
			var a:int = Math.random();
			
			if (a > 10)
			{
				try
				{
					trace("a");
					return;
				}
				catch (e:Error)
				{
					trace("in catch 1");
				}
			}
			else
			{
				try
				{
					trace("b");
				}
				catch (e:Error)
				{
					trace("in catch 2");
				}
			}
			trace("after");
		}
	
	}

}