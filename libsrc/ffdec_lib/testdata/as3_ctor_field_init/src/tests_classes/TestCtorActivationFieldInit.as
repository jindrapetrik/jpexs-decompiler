package tests_classes
{
   /**
    * Named params captured by an inline nested function → NEED_ACTIVATION and
    * getlex of activation slots before super(). Those assigns must stay in the
    * constructor (do not add param aliases / extra locals; that changes the
    * bytecode and no longer hits the case under test).
    */
   public class TestCtorActivationFieldInit
   {
      protected var mValue:Object;
      protected var mFlag:Boolean;
      protected var mInverted:Boolean;
      private var mLiteral:int = 7;

      public function TestCtorActivationFieldInit(value:Object, flag:Boolean = false)
      {
         mValue = value;
         mFlag = flag;
         mInverted = !flag;
         super();
         run(function():Object { return value; });
      }

      private function run(f:Function):void
      {
         f();
      }
   }
}
