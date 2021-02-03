package tests
{
	
	public class TestInlineFunctions
	{
		public function run():*
		{
			var first:String="value1";
			var traceParameter:Function=function(aParam:String):String
			{
				var second:String="value2";
				second=second + "cc";
				var traceParam2:Function=function(bParam:String):String
				{
					trace(bParam + "," + aParam);
					return first + second + aParam + bParam;
				}
				trace(second);
				traceParam2(aParam);
				return first;
			};
			traceParameter("hello");
		}
	}
}
