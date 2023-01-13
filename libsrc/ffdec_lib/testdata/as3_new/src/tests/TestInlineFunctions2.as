package tests
{
	
	public class TestInlineFunctions2
	{
		public function run():*
		{
			function f(a:int):int {
                return a + 1;
            }
            
            var g:Function = function(a:int):int {
                return a + 1;
            };
            
            var h:Function = function h2(a:int):int {
                return a + 1;
            };                        
            
            (function(a:int):int {
                return a + 1;
            })(1);                        
		}
	}
}
