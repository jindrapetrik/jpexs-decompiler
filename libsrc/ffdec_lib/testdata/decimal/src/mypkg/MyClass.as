package mypkg
{    
    public class MyClass
    {        
         private var attr_dec:decimal;
         private var attr_int:int;
    
         public function test(arg_d:decimal): void
         {
            //Presision values: HALF_EVEN, DOWN, FLOOR, UP, CEILING, HALF_UP, HALF_DOWN
            
            //use precision 10, rounding FLOOR;                        
            
            var a:decimal = 10000000010000000002000000000300000000040000000005m;            
         }                    
         
         private function testd(arg_d:decimal) {
         }                     
    }

}
