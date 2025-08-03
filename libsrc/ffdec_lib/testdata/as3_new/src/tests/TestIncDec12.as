package tests
{
	
	public class TestIncDec12
	{	
		public function run():*
		{			    
            var f:Function = function():void {};
            var slot:int = 0;         
            
            trace("slot++ with result");
			trace(slot++);
            
            trace("slot-- with result");
			trace(slot--);      
               
            trace("slot++ no result");
			slot++;
            
            trace("slot-- no result");
			slot--;    
		}
	}
}
