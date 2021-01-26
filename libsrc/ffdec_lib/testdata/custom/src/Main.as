package
{
	import flash.display.Sprite;
	import flash.events.Event;
	import tests.*;
	
	/**
	 * ...
	 * @author JPEXS
	 */
	public class Main extends Sprite 
	{
		TestDupAssignment;
		
		public function Main() 
		{
			if (stage) init();
			else addEventListener(Event.ADDED_TO_STAGE, init);
		}
		
		private function init(e:Event = null):void 
		{
			removeEventListener(Event.ADDED_TO_STAGE, init);
			// entry point
		}
		
	}
	
}