package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryFinallyReturnInFinally
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
			}
			catch (e:Error)
			{
				trace("in catch");
			}
			finally
			{
				trace("in finally");
				if (a > 6){
					return "FINRET1";
				}
				trace("xx");
				if (a > 5){
					return "FINRET2";
				}
				trace("nofinret");
			}
			trace("after");
			return "RETEXIT";
		}
		
	}

}