package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchInWhile5
	{
		
		public function run() : void
		{
			var i:int;
			i = 0;
			var j:int;
			j = 5;
			while(i < 10)
			{
				try
				{
					trace("in try");                
				}
				catch (e:Error)
				{
					if (j > 4) {
						throw new Error("Problem: "+e);
					}
				}
				i++;
			}

			trace("after");
		}
		
	}

}