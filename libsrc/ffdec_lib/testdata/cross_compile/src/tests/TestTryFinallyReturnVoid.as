package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryFinallyReturnVoid
	{
		
		public function run() : void
		{
			trace("before try");
			try
			{
				trace("in try");
				var a:int = 5;
				if (a > 4)
				{
					return;
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
		}
		
	}

}