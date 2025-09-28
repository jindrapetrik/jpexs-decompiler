package tests
{
	
	public class TestSwitchBig
	{
		public function run():*
		{
			var k:* = 10;
			switch (k)
			{
			case "A": 
				trace("A");
                break;
            case "B":
            case "C":
                trace("BC");
                break;       
            case "D":
            default:
            case "E":
                trace("D-default-E");
                break;                                 
			case "F":
                trace("F no break");
            case "G":
                trace("G");
                break;                    			
			case "H": 
				trace("H last");
			}
			trace("after switch");
		}
	}
}
