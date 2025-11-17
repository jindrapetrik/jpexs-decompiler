package tests
{
	
	public class TestIfInsteadSwitch
	{
		public function run():*
		{
		   var a:int = 5;
           if(a > 5)
           {
                if(a === 0)
                {
                    trace("X");
                }
           }
           if(a === 1)
           {
              return "A";
           }
           return "B";     
		}
	}
}
