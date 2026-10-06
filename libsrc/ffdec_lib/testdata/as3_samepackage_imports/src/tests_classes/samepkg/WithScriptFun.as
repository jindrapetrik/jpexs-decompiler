package tests_classes.samepkg
{
   public class WithScriptFun
   {
      public function WithScriptFun()
      {
         super();
         helper();
      }
   }
}

import flash.geom.Point;

function helper():void
{
   var p:Point = new Point(1,2);
   trace(p);
}
