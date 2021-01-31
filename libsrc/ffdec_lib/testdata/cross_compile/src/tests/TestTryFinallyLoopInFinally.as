package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryFinallyLoopInFinally
	{
		
		public function run() : void
		{
			for (var i:int = 0; i < 10; i++)
			{
				trace("before try");
				try
				{
					trace("in try");					
				}
				catch (e:Error)
				{
					trace("in catch");
				}
				finally
				{
					if (i == 5)
					{
						trace("continue for");
						continue;
					}
					trace("in finally");
				}
				trace("after");
			}
		}
		
	}

}