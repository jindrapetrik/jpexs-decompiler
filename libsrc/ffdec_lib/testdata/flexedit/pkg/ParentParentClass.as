package pkg {
	
	public class ParentParentClass {

		//Reference to subclass:  (circular reference)
		private var other:ParentClass;

		public function ParentParentClass() {
			// constructor code
		}
		
		public function setOther(other:ParentClass){
			this.other = other;
		}				

		public function getAChar():String{
			return "A";
		}

	}
	
}
