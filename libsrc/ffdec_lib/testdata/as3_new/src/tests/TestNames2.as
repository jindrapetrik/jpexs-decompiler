package tests
{
	
	public class TestNames2
	{
        public var i:int = 0;
    
		public function run():void
		{
            var j:int = 0;
            var g:Function = null;
			this.i = 0;
            i = 1;
            j = 2;
            trace(this.i);
            trace(i);
            trace(j);
            f();
            this.f();
            g();
		}

        public function f(): void {
        }
	}
}