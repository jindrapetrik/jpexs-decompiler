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
			var i:int = 0;
			while (i < 10)
			{
				trace("before try");
				try
				{
					trace("in try");
					if (i == 5)
					{
						i += 5;
						trace("continue while");
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
				i++;
			}
		}
		
	}

}