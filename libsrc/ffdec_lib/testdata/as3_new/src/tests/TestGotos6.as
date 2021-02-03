package tests
{
	
	public class TestGotos6
	{
		
		public function run():void
		{
			var a:Boolean = true;
			var s:String = "a";
			
			if (a)
			{
				switch (s)
				{
				case "a": 
					trace("is A");
					break;
				case "b": 
					trace("is B");
				case "c":
					trace("is BC");
					break;
				}				
			}
			else
			{
				trace("D");
			}
			trace("finish");
		}
	
	}

}