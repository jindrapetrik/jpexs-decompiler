package tests
{
	
	public class TestIncDec9
	{
        private var attrib:int = 0;
		
		public function run():*
		{			            
            trace("++attrib with result");
			trace(++attrib);
            
            trace("--attrib with result");
			trace(--attrib);      
                
            trace("++attrib no result");
			++attrib;
            
            trace("--attrib no result");
			--attrib;   
		}
	}
}
