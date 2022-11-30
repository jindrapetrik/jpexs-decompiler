package tests
{
	
	public class TestWhileBreak2
	{
		public function run():*
		{
			
            var k:int = 8;
			while(true)
			{
                  trace("X");
                  if (k == 1) {
                      trace("A");
                      return;
                  }
  				  trace("Y");
                  if (k < 10) 
                  {	
                      trace("k1");
                      if (k == 2)
                      {                    
                            trace("B");
                            if (k > 1)
                            {
                                trace("B1");
                                break;
                            }
                            trace("B2");
                      }
                      trace("Z");
                        
                      if (k == 3)
                      {
                            trace("C");
                            break;
                      }
                        
                      trace("Z2");
                        
                      if (k == 4)
                      {
                            trace("D");
                            break;
                      }
                      trace("k2");
                  }
                  trace("E");
  				  if (k == 2) {
                      trace("E1");
                      return;
                  }
  				  trace("gg");
  			}
			trace("ss");		    
		}
	}
}
