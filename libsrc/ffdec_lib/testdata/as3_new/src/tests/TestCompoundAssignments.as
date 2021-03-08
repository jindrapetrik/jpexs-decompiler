package tests
{
	
	public class TestCompoundAssignments
	{
		private var attr :int  = 0;
		
		public function calc():int{
			return 5;
		}
		
		public function run():void
		{
			/*var b:* = [10,20,30];										
			
			var a:int = 0;
			trace("a += 5");
			a += 5;
			
			
			b[calc()] = b[calc()] + 2;
			
			var t:MyTest = new MyTest();
			trace("t.attr += 5");		
			t.attr *= 10;			
			
			trace("attr += 5");	
			attr -= 5;
			
			trace("arr[2] += 5");		
			b[2] += 5;
			
			trace("arr[call()] += 5");				
			b[calc()] /= 5;
			
			b[calc()][calc()] &= 10;
			
			try{
				trace("in try");
			}catch (e:Error){
				trace(e.message);
			}		*/
			var a:int = 0;
			trace(a += 10);
		}
				
	}
}

class MyTest {
	public var attr:int = 0;
}