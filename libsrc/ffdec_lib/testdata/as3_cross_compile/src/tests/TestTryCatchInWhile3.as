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
			trace("before loop"); 
			while (a > 5) 
			{
				try
				{
					return "intry return"; 
				}
				catch(e:Error)
				{
					trace("in catch"); 
				}				
				a++; 
			}			
			return "OK";
		}			
		
	}

}