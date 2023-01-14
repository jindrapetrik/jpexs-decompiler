package tests
{
	
	public class TestSlots2
	{
		public function run():*
		{
			var f:Function = function(): void {
                var n:int = 0;
                try
                {
                    trace("intry");
                }
                catch(e:Error)
                {
                    n = 1;
                }
            };          
		}
	}
}