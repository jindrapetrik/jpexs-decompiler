package tests
{
	
	public class TestWhileBreak3
	{
		public function run():*
		{
            var i:int = Math.floor(Math.random() * 11); //1-14
			while (true) {
				trace("A");     //15-27
                if (i < 100) {
					if (i < 0) { //28-31
						break;
					}
					if (i < 4) {  //32-35
                        break;
					}
				} else {
					trace("C");    //37-42
				}
				if (i == 4) {   //43-46
					trace("D");    //47-55
					return i; 
				}
			}
			return i;    //58-60
		}
	}
}
