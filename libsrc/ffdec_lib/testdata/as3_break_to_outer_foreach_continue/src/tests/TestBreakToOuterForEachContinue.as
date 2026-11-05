package tests
{
   public class TestBreakToOuterForEachContinue
   {
      public var out:String = "";

      public function run(names:Array, keys:Array):void
      {
         for each (var n:String in names)
         {
            for each (var k:String in keys)
            {
               if (k == n)
               {
                  out += k;
                  break;
               }
            }
         }
      }
   }
}
