package tests
{
    import tests_classes.TestConvertParent;
    import flash.utils.getTimer;
	
	public class TestConvert extends TestConvertParent
	{
        private var n:int = 1;
        private var ns:String = "b";
        
        public static var TEST:String = "Hello";
        
        public var TEST:int = 5;
        
		public function run():void
		{
            var s:String;
            var i:int;            
            s = "a";
            i = int(s);
            var j:int = n;
            var a:*;
            s = String(j);
            s = ns;
            
            s = String((i == 4) ? "" : i);
            s = (i == 4) ? "" : String(i);
            
            s = TestConvert.TEST;
            i = this.TEST;
            
            i = Number("4") * 5;
            i = a * 6;
            i = a;
            
            var o:Object = {
            0: "A",
            1: "B",
            2: "C"
            };           
            
            i = int(s.charAt(10));
            
            var v:Vector.<String> = new Vector.<String>();
            v.push("A");
            v.push("B");
            i = int(v[0]);
            s = v[1];
            s = v.join("x");
            i = int(v.join("x"));
            
            i = int(prot);
            s = prot;
            
            s = String(getTimer());            
            
            var x:XML = <list>
	 						<item id="1">1</item>
							<item id="2">2</item>
							<item id="3">3</item>
						</list>;
            s = x;
            trace("a");
		    var xlist:XMLList = x.item;
            trace("b");
            i = int(xlist[i].@id);
            trace("c");
            i = int(x.item[i].@id);
            							
		}
	}
}
