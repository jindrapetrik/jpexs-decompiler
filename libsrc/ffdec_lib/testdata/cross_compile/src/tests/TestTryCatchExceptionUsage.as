package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchExceptionUsage 
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
				trace("catched exception: "+e.message);
			}
			trace("after");
		}
		
	}

}