package tests_classes
{
	import flash.utils.getTimer;
	
	public class TestImports3
	{
        public static function getTimer() : Number 
        {
            return 0;
        }
    
		public function run():*
		{
			return flash.utils.getTimer();
		}
	}
}
