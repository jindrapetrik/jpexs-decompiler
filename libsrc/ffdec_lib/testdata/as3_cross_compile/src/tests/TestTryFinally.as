package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryFinally 
	{
		
		public function run() : void
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
				trace("in finally");
			}
			trace("after");
		}
		
	}

}