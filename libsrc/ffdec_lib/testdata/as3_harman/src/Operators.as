package
{
    public class Operators
    {
        private var a:Object = {x:1, y:2};
        private var b:String = null;
        private var c:String = null;
    
        public function testNullMember() : void {
            var result:*;
            
            result = a?.z;
            
            result = f()?.z;
                        
        }
             
        public function testNullCoalesce() :void {
            var result:String;
            
            result = b ?? "empty";                        
        }
        
        public function f():Object {
            return {z:5};
        }
        
        public function verbatimString(): void {
            var s:String = @"ab\ncd";
        }
    }
}
