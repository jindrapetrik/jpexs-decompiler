package tests_classes
{
   /**
    * Local-register / named-param path: constructor assigns must not become
    * field initializers. Literal slot values may still be promoted.
    */
   public class TestCtorFieldInit
   {
      private var mId:int;
      private var mFlag:Boolean;
      private var mInverted:Boolean;
      private var mName:String;
      private var mLiteral:int = 42;

      public function TestCtorFieldInit(id:int, flag:Boolean = true, name:String = "")
      {
         mId = id;
         mFlag = flag;
         mInverted = !flag;
         mName = name;
         super();
      }

      public function getId() : int
      {
         return this.mId;
      }
   }
}
