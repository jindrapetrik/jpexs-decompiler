package tests
{
	
	public class TestSwitchContinue
	{
		public function run():*
		{
            var r:int = Math.random() % 10;
            
            if (r > 5)
            {
  			    for(var i:int = 0; i < 10; i++)
                {
                    switch(i)
                    {
                        case 0:
                            trace("hello");
                            break;
                        case 1:
                            trace("hi");
                            break;
                        case 2:
                            trace("howdy");
                            break;
                        default:
                            continue;
                    }                
                    trace("message shown");
                }
            }
		}
	}
}
