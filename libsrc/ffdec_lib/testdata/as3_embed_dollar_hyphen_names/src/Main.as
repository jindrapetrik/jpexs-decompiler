package
{
   import flash.display.Sprite;

   public class Main extends Sprite
   {
      [Embed(source="../assets/loading_screen.swf")]
      private static const LoadingScreenSwf:Class;

      public function Main()
      {
         super();
         addChild(new LoadingScreenSwf() as Sprite);
      }
   }
}
