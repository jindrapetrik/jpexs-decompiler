package tests_classes.samepkg
{
   public class Outer
   {
      public function Outer()
      {
         super();
         var helper:Helper = new Helper(new SharedType());
         helper.touch();
      }
   }
}

import tests_classes.samepkg.SharedType;

class Helper
{
   private var shared:SharedType;

   public function Helper(param1:SharedType)
   {
      super();
      this.shared = param1;
   }

   public function touch() : void
   {
      trace(this.shared.tag());
   }
}
