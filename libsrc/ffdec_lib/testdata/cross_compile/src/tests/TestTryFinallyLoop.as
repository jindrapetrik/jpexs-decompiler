package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryFinallyLoop
	{
		
		public function run() : void
		{
			for (var i:int = 0; i < 10; i++)
			{
				trace("before try");
				try
				{
					trace("in try");
					if (i == 5)
					{
						trace("continue for");
						continue;
					}
				}
				catch (e:Error)
				{
					trace("in catch");
				}
				finally
				{
					trace("in finally");
				}
				trace("after");
			}
		}
		
	}

}