package tests
{
	
	public class TestBitwiseOperands
	{
		public function run():*
		{
			var a:int = 100;
            var b:int = a & 0x08ff;
            var c:int = 0x08ff & a;
            var d:int = a | 0x0480;
            var e:int = 0x0480 | a;
            var f:int = a ^ 0x0641;
            var g:int = 0x0641 ^ a;
            var h:int = ~0x0180;
		}
	}
}
