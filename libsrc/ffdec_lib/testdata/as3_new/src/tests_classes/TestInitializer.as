package tests_classes 
{
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestInitializer 
	{
		public static var s_alpha:RegExp = /[a-z]+/;
		public static var s_regs:Array;
		public static var s_numbers:RegExp = /[0-9]+/;
		
		public var i_email:RegExp = /.*@.*\..*/
		public var i_link:RegExp = /<a href=".*">/;		
		public var i_regs:Array = [i_email,i_link];
		
		{
			s_regs = [s_alpha, s_numbers];
		}
		
		public function TestInitializer() 
		{
			trace(s_regs[1]);
		}
		
	}

}