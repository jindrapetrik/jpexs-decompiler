package tests
{
	
	public class TestForEachReturn2
	{
		public function run():*
		{
			var obj:* = null;
            var x:* = 5;            
            if (x != null)
            {
                obj = {};
                for each (var item:* in obj)
                {
                    switch (item["key"])
                    {
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                            return item;
                    };                    
                };
            };
            return null;                        
		}
	}
}
