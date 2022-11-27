package tests
{
    
	public class TestImplicitCoerce
	{
    	public function run():void
		{
            var j:int = 2;
            var i:int = 5;
            var r:* = Math.random();
            
            if (j & Number(r == 1) && 5) {
                trace("OK");
            }  
            var s:String = "hello: " + r;
            
            if (s){
                trace("F");
            }          
		}
	}
}
