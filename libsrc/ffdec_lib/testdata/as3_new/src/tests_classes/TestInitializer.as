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
        
        public var i_a:int = 1;
        public var i_b:int = i_a + 1;
        public const i_c:int = i_a + i_b + 1;
        public var i_d:int;
        		
		{
			s_regs = [s_alpha, s_numbers];
		}
		
		public function TestInitializer(p:int) 
		{
			trace(s_regs[1]);
            i_a = 7;
            i_d = p;
		}
		
	}

}
