package tests
{
	
	public class TestChainedAssignments4
	{
        private var prop:int;
    
		public function run():*
		{
			trace("slotc = slotb = slota = 5;");
			var slota:int = 0;
			var slotb:int = 0;
			var slotc:int = 0;        
            var f:Function = function(n1:int, n2:int):int {return n1 + n2;}; //trigger slot generating    
			slotc = slotb = slota = 5;
		}
	}
}
