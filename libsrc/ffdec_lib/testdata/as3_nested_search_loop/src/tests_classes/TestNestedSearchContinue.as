package tests_classes
{
   /**
    * for each + inner while search with side effect on match then break.
    * ASC merges that break with the outer for-each continue; decompilation
    * must recover while(cond) without inventing loop labels.
    */
   public class TestNestedSearchContinue
   {
      private var items:Array;
      private var rows:Array;
      private var cache:Object;

      public function TestNestedSearchContinue()
      {
         items = [];
         rows = [];
         cache = {};
      }

      public function removeMatches(keys:Array):void
      {
         var i:* = 0;
         for each (var key in keys)
         {
            i = 0;
            while (i < items.length)
            {
               if (items[i].key == key)
               {
                  items.splice(i, 1);
                  break;
               }
               i++;
            }
         }
      }

      public function fillCache(entries:Array):void
      {
         var row:Object = null;
         var i:int = 0;
         var allRows:Array = rows;
         for each (var entry in entries)
         {
            if (!(entry.flag && !allowFlagged()))
            {
               i = 0;
               while (i < allRows.length)
               {
                  row = allRows[i];
                  if (row.kind == "TYPE_A" && row.place == "ZONE_B" && !row.grouped)
                  {
                     if (row.ref == entry.key)
                     {
                        cache[entry.key] = row;
                        break;
                     }
                  }
                  i++;
               }
            }
         }
      }

      private function allowFlagged():Boolean
      {
         return false;
      }
   }
}
