package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchInIf
	{
		
		public function run() : int
		{
			var a:int = Math.random();
			if (a > 10)
			{
				try
				{
					return 1;
				}
				catch(e:Error)
				{
					// ignore
				}
			}
			return 2;
		}
		
	}

}