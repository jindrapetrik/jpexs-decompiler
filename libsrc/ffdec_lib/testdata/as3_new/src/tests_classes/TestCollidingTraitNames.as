package tests_classes
{
	public class TestCollidingTraitNames extends CollidingAttributeParent
	{
		public var CollidingAttribute:tests_classes.CollidingAttribute;
        
        public function test(): void
        {
            var t:tests_classes.CollidingAttribute2 = null;   
        }
        
        public function CollidingMethod(): void {
            var t:tests_classes.CollidingMethod = null;
        }
	}
}
