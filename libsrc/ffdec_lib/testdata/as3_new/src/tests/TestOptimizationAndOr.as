package tests
{
	
	public class TestOptimizationAndOr
	{
		public function run():*
		{        
            var o:Object = {a:"Object", b:"Object", c:"Object"};
            
            var a:String = "d";
            
            var plugin:Object;
            
            if (a in o && (plugin = new o[a]).toString().length > 2) {
                trace("okay");
            }              
		}
	}
}
