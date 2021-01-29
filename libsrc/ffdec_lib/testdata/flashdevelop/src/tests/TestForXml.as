package tests
{
   public class TestForXml
   {
       
      
      public function run() : *
      {
          var name:String = "ahoj";
          var myXML:XML=<order id="604">
                        <book isbn="12345">
                        <title>{name}</title>
                        </book>
                      </order>;

			var k:* = null;            
            
            var len:int = 5;
            var a:int = 5;
            var b:int = 6;
            
            for (var i:int = 0; i < len; k=myXML.book.(@isbn =="12345"))
            {
                var c:int = 1;
    
                if (c == 2)
                    trace("A")
                else if (c == 3)
                    trace("B")
                else
                    continue;

                trace("C")                  
                
            }
      }

   }
}