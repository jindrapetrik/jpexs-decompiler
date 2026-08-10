package
{
   import flash.display.Sprite;
   import tests_classes.TestCtorFieldInit;
   import tests_classes.TestCtorActivationFieldInit;
   import tests_classes.TestCtorDependsOnInstanceSlot;

   public class Main extends Sprite
   {
      public function Main()
      {
         super();
         new TestCtorFieldInit(1, true, "x");
         new TestCtorActivationFieldInit({}, false);
         new TestCtorDependsOnInstanceSlot({});
      }
   }
}
