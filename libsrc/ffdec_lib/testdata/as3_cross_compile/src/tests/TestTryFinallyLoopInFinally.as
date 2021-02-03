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
			var i:int = 0;
			while (i < 10)
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
						i += 7;
						trace("continue while");
						continue;
					}
					trace("in finally");
				}
				trace("after");
				i++
			}
		}
		
	}

}