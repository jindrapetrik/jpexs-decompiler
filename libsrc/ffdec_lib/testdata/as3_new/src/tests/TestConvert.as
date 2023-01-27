package tests
{
    import tests_classes.TestConvertParent;
    import flash.utils.getTimer;
    import flash.utils.Dictionary;
	
	public class TestConvert extends TestConvertParent
	{
        private var n:int = 1;
        private var ns:String = "b";
        
        public static var TEST:String = "Hello";
        
        public var TEST:int = 5;
        
        private var f:Function = null;
        
		public function run():void
		{
            var s:String;
            var i:int;   
            var dict:Dictionary = new Dictionary();     
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
            
            i = prot;
            s = String(prot);
            
            i = sprot;
            s = String(sprot);
            
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
            
            dict[String(x.item[i].@id)] = "Hello";
            				
            var lc:LocalClass = new LocalClass();
            i = lc.attr;      
            s = String(lc.attr);
            
            
            var f:Function = this.f;
            if (Boolean(f)) {
                trace("OK");
            }
            
            if (i) {
                trace(i);
            }
            if (s) {
                trace(s);
            }
        
            if (o) {
                trace("obj");
            }             
            s = xlist;
            var d:Number = 0;
            d = 1;
            d = 1.5;
            i = 1;
            i = 1.5;  
            o[int(d * 5)] = 1;
            this.n = 1.5;
            super.prot = 1.5;
            super.prot = int(s);
            i = super.prot;
            s = String(super.prot);      
            			
		}
	}
}

class LocalClass{
    public var attr:int = 5;
}