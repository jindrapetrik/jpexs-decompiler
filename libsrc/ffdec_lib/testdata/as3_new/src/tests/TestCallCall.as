package tests
{
   import flash.utils.getDefinitionByName;
   
   public class TestCallCall
   {
      public function run() : *
      {
         var o:* = new getDefinitionByName("Object")();
         var o2:* = new (getDefinitionByName("Object"))();
      }	
   }
}
