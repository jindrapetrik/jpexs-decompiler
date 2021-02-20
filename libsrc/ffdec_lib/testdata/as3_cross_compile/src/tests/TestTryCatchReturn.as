package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchReturn
	{
		
		public function run() : int
		{
			var a:int;
			a = 5;
			trace("before try");
			try
			{
				trace("in try");
			}
			catch (e:Error)
			{
				trace("in catch");				
				if (a == 5)
				{
					return a;
				}
				trace("in catch2");
			}			
			trace("after");
			return -1;
		}
		
	}

}