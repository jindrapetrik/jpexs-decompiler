package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryFinallyReturn
	{
		
		public function run() : String
		{
			trace("before try");
			try
			{
				trace("in try");
				var a:int = 5;
				if (a > 4)
				{
					return "RET";
				}
				trace("between");
				if (a < 3)
				{
					return "RE2";
				}
				trace("in try2");
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
			return "RETFINAL";
		}
		
	}

}