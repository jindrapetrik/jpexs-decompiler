package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryFinallyNoCatch
	{
		
		public function run() : void
		{
			trace("before try");
			try
			{
				trace("in try");
			}
			finally
			{
				trace("in finally");
			}
			trace("after");
		}
		
	}

}