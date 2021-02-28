package tests
{
	import flash.utils.Dictionary;
	
	public class TestForInReturn
	{
		public function run():*
		{
			var dic:Dictionary = null;
			var item:* = null;
			for (item in dic)
			{
				return item;
			}
			return null;
		}
	}
}
