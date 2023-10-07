class com.jpexs.MyClass {
    var v;
    static var sv;
    var v2;  
    static var sv2;
    
    var f;
    
    var c;
    
    var d;    
    
    var init_v = 2;
    static var sinit_v = 3;
    
    function testVar() {
        this.v = 1;
    }
    
    function getV2() {
        return v2;
    }
    
    static function getSV2() {
        return sv2;
    }
    
    function callF() {
        f();
    }
    
    function constructC() {
        new c();
    }
    
    function deleteD() {
        delete d;
    }
}
