package tests
{
	
	public class TestFinallyZeroJump
	{
		public function run(param1:String) : String
		{
			var str:String = param1;
			try
			{
			}
			catch (e:Error)
			{
				trace("error is :" + e.message);
			}
			finally
			{
				trace("hi ");
				if (5 == 4)
				{
					return str;
				}
				return "hu" + str;
			}
		}
	}
}
