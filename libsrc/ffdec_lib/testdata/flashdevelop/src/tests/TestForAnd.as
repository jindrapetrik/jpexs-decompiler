package tests
{
   public class TestForAnd
   {
       
      
      public function run() : *
      {
          var len:int = 5;
          var x : Boolean;
          var a:int = 4;
          var b:int = 7;
          var c:int = 9;
          for (var i:uint = 0; i < len; x = a > 4 && b < 2 || c > 10)
          {
              c = 1;
  
              if (c == 2) {
                  trace("A");
                  if (c == 7)
                  {
                      trace("B");
                      continue;
                  }
                  trace("C");
              }                
              trace("D");                  
              
          }
      }

   }
}