package tests
{
   public class TestTryAfterCompoundContinue
   {
      public var out:String = "";

      public function run(args:Array):void
      {
         for each (var s:String in args)
         {
            if (s != "a" && s != "b")
            {
               continue;
            }
            try
            {
               if (s.length > 5)
               {
                  throw new Error("long");
               }
               out += s;
            }
            catch (error:Error)
            {
               out += error.message;
            }
            return;
         }
      }
   }
}
