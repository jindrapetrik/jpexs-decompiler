package mypkg
{    
    [API("661")]
    [API("662")]
    public class MyClass
    {        
         public function test(): void
         {
            trace("Hello world");                  
         }
         
         [API("662")]
         public function test_662(): void
         {
            trace("Hello world");                  
         }
         
         [API("674")]
         public function test_674(): void
         {
            trace("Hello world");                  
         }
         
         [API("662")]
         [API("674")]
         public function test_662_674(): void
         {
            this.test_674();
            trace("Hello world");                  
         }
    }

}
