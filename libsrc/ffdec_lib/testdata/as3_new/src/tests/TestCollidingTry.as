package tests
{
	
	public class TestCollidingTry
	{
		public function run():*
		{
			var e:int = 0;
            try
            {
                e = 0;
            }
            catch(e:*)
            {
                trace(e);
            }
            try
            {
                trace("x");
            }
            catch(e:*)
            {
                trace(e);
            }
            trace("y");
		}
	}
}
