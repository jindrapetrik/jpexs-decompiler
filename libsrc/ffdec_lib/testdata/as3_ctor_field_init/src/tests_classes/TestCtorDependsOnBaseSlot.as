package tests_classes
{
   /**
    * Ctor assigns an inherited instance slot, then derives a field from it.
    * That derived assign must stay in the constructor: field initializers run
    * before super(), so the inherited slot is still at its default.
    */
   public class TestCtorDependsOnBaseSlot extends TestCtorDependsOnBaseSlotBase
   {
      public var mText:String;

      private var mLiteral:int = 9;

      public function TestCtorDependsOnBaseSlot(origin:Object)
      {
         mOrigin = origin;
         mText = String(mOrigin);
         super(origin);
      }
   }
}
