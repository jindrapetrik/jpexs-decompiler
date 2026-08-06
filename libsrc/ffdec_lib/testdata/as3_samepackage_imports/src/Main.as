package
{
   import flash.display.Sprite;
   import tests_classes.samepkg.Outer;
   import tests_classes.samepkg.WithScriptFun;

   public class Main extends Sprite
   {
      public function Main()
      {
         super();
         new Outer();
         new WithScriptFun();
      }
   }
}
