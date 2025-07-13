package tests
{
	
	public class TestLoopInLoop
	{
		public function run():*
		{
			var a:Boolean = true;
    		var b:Boolean = true;
    		var c:Boolean = true;
    		
    		
    		for (;;)
    		{
    			trace("A");
    			
    			for (var i = 0; i < 10; i++)
    			{
    				if (a)
    				{
    					continue;
    				}
    				
    				trace("B");
    				if (c)
    				{
    					trace("C");
    				}
    				else
    				{
    					trace("D");
    					if (b)
    					{
    						continue;
    					}
    										
    					trace("H");					
    					
    				}
    				
    				if (c)
    				{
    					trace("L");
    				}
    			}
    			
    			if (a)
    			{
    				break;
    			}			
    		}           
		}
	}
}
