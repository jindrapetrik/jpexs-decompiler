package tests
{
	
	public class TestWhileDoWhile
	{
		public function run():*
		{
            trace("A");
            var i:int = 0;
            
			while (i < 10)
            {
                trace("B");
                do 
                {
                    i++;
                    trace("C");
                } while (i < 5);
            }
            trace("E");   
            
		}
	}
}
