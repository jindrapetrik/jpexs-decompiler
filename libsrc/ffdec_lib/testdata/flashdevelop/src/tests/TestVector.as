package tests
{
	
	public class TestVector
	{
		public function run():*
		{
			var v:Vector.<String> = new Vector.<String>();
			v.push("hello");
			v[0] = "hi";
			var a:int = 5;
			v[a * 8 - 39] = "hi2";
			trace(v[0]);
		}
	}
}
