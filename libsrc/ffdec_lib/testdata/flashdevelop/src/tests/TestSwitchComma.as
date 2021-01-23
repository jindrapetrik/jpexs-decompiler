package tests 
{
	public class TestSwitchComma 
	{
		private static const X:int = 7;
		
		public function run():*
		{
			var b:int = 5;
			
			var a:String = "A";
			switch (a)
			{
			case "A": 
				trace("is A");
				break;
			case "B": 
				trace("is B");
			case TestSwitchComma.X,"C":
				trace("is C");
				break;			
			}
		}		
		
	}

}