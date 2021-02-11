package tests_classes 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestRegexpHilight 
	{
		
		public function run():void
		{
			var myregexp:RegExp = /[a-z0-9_]+/;
			var a:Number = 10;
			var b:Number = 20;
			var notaregexp:Number = a / b + b / a;
			
			trace(myregexp);
			trace(notaregexp);
		}
		
	}

}