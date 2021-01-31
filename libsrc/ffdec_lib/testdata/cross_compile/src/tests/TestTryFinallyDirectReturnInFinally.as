package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryFinallyDirectReturnInFinally 
	{
		
		public function run() : String
		{
			
			var str:String = "xxx";
			try
			{
			}
			catch (e:Error)
			{
				trace("error");
			}
			finally
			{
				trace("hi ");
				if (str == "check")
				{
					return str;
				}
				return "hu" + str;
			}
		}
		
	}

}