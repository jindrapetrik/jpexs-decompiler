package tests
{
	import flash.utils.Dictionary;
    
	public class TestOperations
	{
		public function run():*
		{
            var c:MyClass = new MyClass();            
            var cr:MyClass;            
            var d:Dictionary = new Dictionary();
            var n1:Number = 2;
            var n2:Number = 3;
            var b1:Boolean = true;
            var b2:Boolean = false;
            var br:Boolean;            
            var r:Number;
            var v:*;
            var x:XML = <a>
            			<b>one
            				<c> 
            					<b>two</b> 
            				</c> 
            			</b>
            			<b>three</b>
            		</a>;    
            var xlr:XMLList;  
            var o:Object = {a:1, b:2}; 
            var sr:String;  
            var s1:String = "hello";
            var s2:String = "there";   
            
            
            r = -n1;
            r = ~n1;
            br = !b1;
            r = ++n1;
            r = n1++;
            
            cr = c as MyClass;            
            br = "hello" in d;            
            
            r = b1 ? n1 : n2;
            r = n1 << n2;
            r = n1 >> n2;
            r = n1 >>> n2;
            r = n1 & n2;
            r = n1 | n2;
            r = n1 / n2;
            r = n1 % n2;
            br = n1 == n2;
            br = n1 === n2;
            br = n1 != n2;
            br = n1 !== n2;
            br = n1 < n2;
            br = n1 <= n2;
            br = n1 > n2;
            br = n1 >= n2;
            br = b1 && b2;
            br = b1 || b2;
            r = n1 - n2;
            r = n1 * n2;
            r = n1 + n2;
            r = n1 ^ n2;
            br = c instanceof MyClass;
            br = c is MyClass;
            xlr = x..b;
            sr = s1 + s2;                        
                        
            r &= n1;
            r |= n1;
            r /= n1;
            r -= n1;
            r %= n1;
            r *= n1;
            r += n1;
            r <<= n1;
            r >>= n1;
            r >>>= n1;
            r ^= n1;
            br &&= b1;
            br ||= b1; 
            sr += s1;
                      
            delete o.a;
            v = void("test" + this.f()); //TODO: implement compiling this
            sr = typeof c;
                   
		}
        
        public function f():int {
            trace("f");
            return 5;
        }
	}
}

class MyClass {
}