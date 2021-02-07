package tests 
{
	import flash.errors.EOFError;
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchInWhile3
	{
		
		public function run() : String
		{
			var a:int;
			a = 0;			
			trace("before loop"); //1-17
			while (a > 5) //44-47
			{
				try
				{
					return "intry return"; //20-23
				}
				catch(e:Error)
				{
					trace("in catch"); //26-43
				}				
				a++; //26-43 cont.
			}			
			return "OK";//48-50
		}			
		
	}

}