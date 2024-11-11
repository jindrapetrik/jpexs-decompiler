package
{
    public class Operators
    {
        private var a:Object = {x:1, y:2};
        private var b:String = null;
    
        public function testNullMember() : void {
            var result:*;
            
            result = a?.z;
        }
             
        public function testNullCoalesce() :void {
            var result:String;
            
            result = b ?? "empty";
        }
    }
}
