package tests
{
	
	public class TestWhileSwitch
	{
		public function run():*
		{
           var a:Boolean = true;
           var d:int = 5;
           var e:Boolean = true;
           var i:int = 0;
           while(i < 100)
           {
              trace("start");
              if(a)
              {
                 trace("A");
              }
              else
              {
                 switch(d)
                 {
                    case 1:
                       trace("D1");
                 }
              }
              if(e)
              {
                 trace("E");
              }
              i++;
           }         
		}
	}
}
