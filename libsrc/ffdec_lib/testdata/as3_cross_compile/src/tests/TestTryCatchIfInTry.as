package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchIfInTry 
	{
		
		public function run() : void
		{
			var a:Boolean = true;
			trace("before");
			try
			{
				if (a)
				{
					trace("ret");
					return;
				}
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