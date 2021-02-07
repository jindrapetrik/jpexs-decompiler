package tests
{
	
	public class TestWhileBreak
	{
		public function run():*
		{
			var a:int = 0;			
			while (a < 10)
            {
                if (a > 1 && a > 2 && a > 3 && a > 4 && a > 5){
                    return "A";
                }
                trace("middle");
                if (a == 5){
					break;
				}				
            };
            return "B";
		}
	}
}
