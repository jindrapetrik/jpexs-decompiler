package tests
{
	
	public class TestVector
	{
		public function run():*
		{
			var v:Vector.<String> = new Vector.<String>();
			v.push("hello");
			v[0] = "hi";
			v[5 * 8 - 39] = "hi2";
			trace(v[0]);
		}
	}
}
