package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatch
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
			trace("after");
		}
		
	}

}