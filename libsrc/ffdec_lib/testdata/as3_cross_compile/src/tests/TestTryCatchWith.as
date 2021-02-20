package tests 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchWith
	{
		
		public function run() : void
		{
			var a:MyTest = new MyTest();
			
			trace("before with");
			with (a)
			{
				trace("before try");			
				try
				{
					trace("in try");
				}
				catch (e:Error)
				{
					attrib = attrib + 1;
					trace("in catch");				
				}			
				trace("after try");
			}
			trace("after");
		}
		
	}

}

class MyTest 
{
	public var attrib:int = 5;
}