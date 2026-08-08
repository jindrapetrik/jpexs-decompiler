package tests_classes
{
   /**
    * for each + inner while search with side effect on match then break.
    * ASC merges that break with the outer for-each continue; decompilation
    * must recover while(cond) without inventing loop labels.
    */
   public class TestNestedSearchContinue
   {
      private var blocked:Array;
      private var offers:Array;
      private var map:Object;

      public function TestNestedSearchContinue()
      {
         blocked = [];
         offers = [];
         map = {};
      }

      public function refresh(list:Array):void
      {
         var i:* = 0;
         for each (var id in list)
         {
            i = 0;
            while (i < blocked.length)
            {
               if (blocked[i].id == id)
               {
                  blocked.splice(i, 1);
                  break;
               }
               i++;
            }
         }
      }

      public function loadOffers(heroes:Array):void
      {
         var offer:Object = null;
         var i:int = 0;
         var allOffers:Array = offers;
         for each (var hero in heroes)
         {
            if (!(hero.hidden && !wantHidden()))
            {
               i = 0;
               while (i < allOffers.length)
               {
                  offer = allOffers[i];
                  if (offer.tab == "HERO" && offer.location == "STORE" && !offer.isBundle)
                  {
                     if (offer.heroId == hero.id)
                     {
                        map[hero.id] = offer;
                        break;
                     }
                  }
                  i++;
               }
            }
         }
      }

      private function wantHidden():Boolean
      {
         return false;
      }
   }
}
