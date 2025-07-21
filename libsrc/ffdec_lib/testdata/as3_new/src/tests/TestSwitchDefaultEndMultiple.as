package tests
{
	
	public class TestSwitchDefaultEndMultiple
	{
		public function run():*
		{
			var a:* = "X";
			switch (a)
			{
  			   case "A":
                    trace("A");
                    break;
               case "B":
                    trace("B");
                    break;
               case "C":
               case "D":
               default:
            }
		}
	}
}
