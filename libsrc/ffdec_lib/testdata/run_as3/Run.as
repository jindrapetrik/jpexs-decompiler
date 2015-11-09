package 
{

	public class Run
	{

		public static function run():*
		{
			return new Run().runInstance();
		}

		public function runInstance():*
		{
			return "Test" + executeMethod("testInstance") + executeStaticMethod("testStatic");
		}

		public function executeMethod(methodName:String):String
		{
			try
			{
				var result = this[methodName]();
				return methodName + "_Result:" + result + " Type:" + typeof(result) + "\n";
			}
			catch (ex)
			{
				return methodName + "_Error:" + ex + "\n";
			}

			return null;
		}

		public static function executeStaticMethod(methodName:String):String
		{
			try
			{
				var result = Run[methodName]();
				return "Result:" + result + " Type:" + typeof(result) + "\n";
			}
			catch (ex)
			{
				return "Error:" + ex + "\n";
			}
			
			return null;
		}

		public function testInstance()
		{
			return "testInstance";
		}

		public static function testStatic()
		{
			return "testStatic";
		}
	}
}