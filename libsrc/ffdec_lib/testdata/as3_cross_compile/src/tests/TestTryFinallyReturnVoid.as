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
			var a:int = Math.random() * 5;
			trace("before try");
			try
			{
				trace("in try");
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