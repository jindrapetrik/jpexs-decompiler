package tests
{
	
	public class TestAlwaysBreak
	{
		public function run():*
		{
			while(true)
            {
               var v:int = 5;
               trace("a");
               if(v > 4)
               {
                  trace("b");
                  if(v > 10)
                  {
                     trace("c");
                     break; //standard "break", should lead to "f"
                  }
                  else
                  {
                     trace("d");
                  }
               }
               trace("e");
               break; //"always break loop"
            }
            trace("f");
		}
	}
}
