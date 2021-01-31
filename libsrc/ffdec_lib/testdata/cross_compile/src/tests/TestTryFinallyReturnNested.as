package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryFinallyReturnNested 
	{
		
		public function run() : String
		{
			try
			{
				trace("before try2");
				try
				{
					trace("in try2");
					var a:int = 5;
					if (a > 4)
					{
						return "RET";
					}
				}
				catch (e:Error)
				{
					trace("in catch");
				}
				finally
				{
					trace("in finally2");
				}
				trace("after");
			}
			finally
			{
				trace("in finally1");
			}
			return "RETFINAL";
		}
		
	}

}