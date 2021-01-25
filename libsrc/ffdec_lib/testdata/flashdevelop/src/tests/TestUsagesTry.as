package tests 
{
	public class TestUsagesTry 
	{
		
		public function run() : String
		{
				var k:int = 5;
				switch(k){
					case 0: trace("1"); break;
					case 1: trace("2"); break;
				}
				
				var a:Boolean = true;
				var b:Boolean = true;
				try
				{
					if (b) {
						return "B";
					}	
					trace("A");
				}
				catch (e:Error)
				{
					trace("E");
				}
				finally
				{				
					trace("finally");				
				}
				trace("after");
				return "X";
		}
		
	}

}