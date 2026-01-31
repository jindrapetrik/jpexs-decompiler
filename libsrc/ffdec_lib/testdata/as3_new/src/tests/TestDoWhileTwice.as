package tests
{
	
	public class TestDoWhileTwice
	{
		public function run():*
		{
			var a = 1;
            var b = 2;
            do {
            	do {
            		if (a) {
            			trace("x");
            			if (b) {
            				break;
            			}
            			trace("y");
            		}
            		trace("z");
            	}while(true);
            	trace("g");
            	if (b) {
            		break;
            	}
            	trace("h");
            }while(true);       
            trace("finish");      
		}
	}
}
