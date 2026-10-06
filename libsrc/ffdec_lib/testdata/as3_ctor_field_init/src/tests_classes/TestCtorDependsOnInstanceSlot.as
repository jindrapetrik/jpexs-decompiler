package tests_classes
{
   /**
    * Ctor assigns an instance slot, then derives other fields from it.
    * Those derived assigns must stay in the constructor: field initializers
    * run before the ctor body, so the source slot is still null.
    */
   public class TestCtorDependsOnInstanceSlot
   {
      protected var mSource:Object;

      public var mText:String;

      public var mNote:String;

      private var mLiteral:int = 3;

      public function TestCtorDependsOnInstanceSlot(data:Object)
      {
         mSource = data;
         mText = String(mSource);
         mNote = mSource == null ? "" : "x";
         super();
      }

      public function getSource() : Object
      {
         return this.mSource;
      }
   }
}
