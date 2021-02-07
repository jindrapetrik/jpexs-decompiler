package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchTry
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
				try
				{
					trace("in catch try");
				}
				catch (e2:Error)
				{
					trace("in catch in catch");
				}
			}			
			trace("after");
		}
		
	}

}