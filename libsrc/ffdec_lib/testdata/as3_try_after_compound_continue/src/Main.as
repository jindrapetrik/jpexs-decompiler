package
{
   import flash.display.Sprite;
   import tests.TestTryAfterCompoundContinue;

   public class Main extends Sprite
   {
      public function Main()
      {
         new TestTryAfterCompoundContinue().run(["a"]);
      }
   }
}
