package tests
{
	
	public class TestInc2
	{
		public function run():*
		{
			var a:* = [1];			
			var d:* = a[this.getInt()]++;
			var e:* = ++a[this.getInt()];
			
			a[this.getInt()]++;
			++a[this.getInt()];
			
			var b:* = 1;
			b++;
			var c:* = 1;
			b = c++;
		}
		
		private function getInt():int
		{
			return 5;
		}
	}
}
