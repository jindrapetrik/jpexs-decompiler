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
            var x:XML = <a>
            			<b>one
            				<c> 
            					<b>two</b> 
            				</c> 
            			</b>
            			<b>three</b>
            		</a>;    
            var xlr:XMLList;        
            
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
            
                   
		}
	}
}

class MyClass {
}