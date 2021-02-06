package tests 
{
	import flash.errors.EOFError;
	/**
	 * ...
	 * @author JPEXS
	 */
	public class TestTryCatchLoopBreak
	{
		
	
		public function run() : void
		{
			var a:int;
			a = 0;			
			trace("before loop");//1-15
			try
			{
				trace("try1a");//16-22
				while (a < 10){ //34-37			loop0
					trace("a=" + a); //23-33
					a++;
				}
				trace("try1b"); //38-44
				//45-45
			}
			catch(e:Error)
			{
				trace("in catch"); //46-61
			}				

			trace("middle");//62-68
			
			
			while (a < 20) { //104-107			loop1
				//69-69
				try
				{
					trace("try2");	//70-77
					return;
				}
				catch(e:Error)
				{
					trace("in catch2"); //80-103
				}
				trace("a=" + a); //pokračuje 80-103
			}
			trace("middle2"); //108-114
			
			while (true) { //161-161 			loop2				
				//115-115
				try
				{
					trace("try3"); //116-122
				}
				catch(e:Error)
				{
					trace("in catch3"); //124-141
					break;
				}	
				catch(e:EOFError)
				{
					trace("in catch4"); //141-159
					break;
				}
				//123-123
			}
			trace("exit"); //162-167
			
		}
		
	}

}