package mypkg
{    
    use decimal, rounding CEILING, precision 10;
    
    public class MyClass/*!*/
    {        
         private var attr_dec:decimal;
         private var attr_int:int;
    
         public function test(arg_d:decimal): void
         {
            //Precision values: HALF_EVEN, DOWN, FLOOR, UP, CEILING, HALF_UP, HALF_DOWN                                                
            
            var a:decimal = 10000000010000000002000000000300000000040000000005m;
            var b:int = 10;
            var c:*;
            c = a + b;
            c = a - b;
            c = a / b;
            c = a % b;
            c = a * b;
            ++c;
            --c;
            c = -a;
         }                                 
    }

}
