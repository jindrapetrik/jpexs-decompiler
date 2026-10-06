package tests
{
   import lib.Cont;
   import lib.Item;

   /**
    * Untyped local gets the call return type inferred as IIt with no coerce
    * in ABC — that type must still be imported.
    * Also covers Vector.<T> receivers (ApplyTypeAVM2Item) for callproperty.
    */
   public class User
   {
      private var c:Cont = new Cont();
      private var items:Vector.<Item> = new Vector.<Item>();

      public function go() : void
      {
         var it = c.iterator();
         if (it != null && it.hasNext())
         {
            it.next();
         }
         var n = items.join(",");
      }
   }
}
