package tests_classes
{
   /**
    * Parent declares an instance slot assigned only in constructors.
    */
   public class TestCtorDependsOnBaseSlotBase
   {
      protected var mOrigin:Object;

      public function TestCtorDependsOnBaseSlotBase(origin:Object)
      {
         mOrigin = origin;
      }
   }
}
