package tests
{	
	public class TestForEachTry
	{
		public function run():*
		{
			 var list = {};
             var b = true;
             for each(var name in list)
             {
                try
                {
                   trace("xx");
                   if(b)
                   {
                      trace("A");
                   }
                   else
                   {
                      trace("B");
                   }
                }
                catch(e:Error)
                {
                   trace("C");
                }
                trace("D");
             }
		}
	}
}
