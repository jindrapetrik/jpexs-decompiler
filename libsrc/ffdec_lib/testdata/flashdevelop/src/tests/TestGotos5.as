package tests 
{
	
	public class TestGotos5 
	{
		
		public function run() : void
		{
			var s:String = "A";
			var i:int = 0;
			for(; i < 10; i++)
			{
			   if(s == "B")
			   {
				  if(s == "C")
				  {
					 continue;
				  }
			   }
			   trace("D");
			   var j:int = 0;
			   while(j < 29)
			   {
				  trace("E");
				  j++;
			   }
			}
		}
		
	}

}