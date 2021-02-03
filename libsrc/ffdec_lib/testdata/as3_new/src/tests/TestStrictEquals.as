package tests 
{

	public class TestStrictEquals 
	{
		
		public function run() : void 
		{
			var k:int = 6;
			if (this.f() !== this.f()){
				trace("is eight");
			}
		}
		
		private function f():String {
			return "x";
		}
		
	}

}