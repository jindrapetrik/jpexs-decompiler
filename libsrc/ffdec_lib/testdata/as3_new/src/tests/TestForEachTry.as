package tests
{	
	public class TestForEachTry
	{
		public function run():*
		{
			 var list:Object = {};
             var b:Boolean = true;
             for each(var name:String in list)
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
