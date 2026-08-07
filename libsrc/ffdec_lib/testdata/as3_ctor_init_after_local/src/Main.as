package
{
   import flash.display.Sprite;
   import tests_classes.TestCtorInitAfterLocal;

   public class Main extends Sprite
   {
      public function Main()
      {
         new TestCtorInitAfterLocal();
      }
   }
}
