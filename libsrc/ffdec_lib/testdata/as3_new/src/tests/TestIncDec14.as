package tests
{
	
	public class TestIncDec14
	{	
		public function run():*
		{			    
            var a:* = [1,2,3,4,5];
        
            trace("a[this.f()]++ with result");
			trace(a[this.f()]++);
            
            trace("a[this.f()]-- with result");
			trace(a[this.f()]--);
            
            trace("a[this.f()]++ no result");
			a[this.f()]++;
            
            trace("a[this.f()]-- no result");
			a[this.f()]--;      
		}
        
        private function f():int
		{
			return 0;
		}
	}
}
