package tests
{
   public class TestCatchVariableNamedAsMethod
   {
      public static var out:String = "";

      public static function error(message:String):void
      {
         out += message;
      }

      public function run():void
      {
         try
         {
            error("a");
         }
         catch (error:Error)
         {
            out += error.message;
         }
      }
   }
}
