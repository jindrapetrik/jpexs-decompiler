package tests
{
	
	public class TestStringCoerce
	{
        var a:Object = null;
    
		public function run():*
		{
			var text1:String = this.a["test"];
            var text2:String = String(this.a["test"]);
		}
	}
}
