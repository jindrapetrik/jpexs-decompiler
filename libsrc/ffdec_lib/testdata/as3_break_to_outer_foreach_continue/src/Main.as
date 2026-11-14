package
{
   import flash.display.Sprite;
   import tests.TestBreakToOuterForEachContinue;

   public class Main extends Sprite
   {
      public function Main()
      {
         new TestBreakToOuterForEachContinue().run(["a"], ["a"]);
      }
   }
}
