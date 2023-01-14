package tests
{
	
	public class TestSlots
	{
		public function run():*
		{
			var i:int = 1;
            var f:Function = function():void
              {
                 trace("hello");
              };
            i = 0;
            trace(i++);
            trace(i--);
            trace(++i);
            trace(--i);            
		}
	}
}