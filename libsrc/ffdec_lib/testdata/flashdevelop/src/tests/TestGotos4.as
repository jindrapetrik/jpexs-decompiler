package tests 
{
	public class TestGotos4 
	{
		
		public function run() : void
		{
			var a:int = 5;
			if(a > 3)
			{
				if(a < 7)
				{
				   try
				   {
					   trace("A");
				   }
				   catch(error:Error)
				   {
				   }
				   trace("B");
				}		
			}
			trace("return");			 
		}
		
	}

}