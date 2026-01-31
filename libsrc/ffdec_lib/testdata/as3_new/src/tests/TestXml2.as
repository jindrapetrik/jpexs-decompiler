package tests
{
	
	public class TestXml2
	{
		public function run():*
		{
			var x1:XML = new XML("<elem name=\"aaa\" value=\"xxx\n\"/>");
            var x2:XML = <elem 
                name="aaa" 
                value="xxx"
                />;
            var x3:XML = <elem 
                name="aaa" value="xxx">
                <sub title="yyy">
                    ampersand: &amp;
                </sub>    
                <sub />            
            </elem>;
            
            var x4:XML = <elem>
                <elem>
                    A
                </elem>
                <elem>
                    B
                </elem>
                <elem>
                    <elem>
                        C
                    </elem>
                </elem>
            </elem>;
            
            var x5:XML = <elem attr="abc\r\n\tdef"></elem>;
            
            var x_invalid:XML = new XML("<aaa >> invalid \"\n");
            
            var a:int = 5;
            trace("B");           
		}
	}
}
