package tests
{
   public class TestForGoto
   {
       
      
      public function run() : *
      {
          var len:int = 5;
          for (var i:uint = 0; i < len; ++i)
          {
              var c:int = 1;
  
              if (c == 2)
                  trace("A")
              else if (c == 3)
                  trace("B")
              else
                  continue;
  
              trace("C")                  
              
          }
          trace("exit");
      }

   }
}