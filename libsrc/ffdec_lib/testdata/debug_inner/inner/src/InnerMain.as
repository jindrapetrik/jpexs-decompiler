package
{
	import flash.display.Sprite;
	import flash.events.Event;
		
	public class InnerMain extends Sprite 
	{
		MyInnerClass;
		
		public function InnerMain() 
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