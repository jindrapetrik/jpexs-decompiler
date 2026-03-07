class com.jpexs.flash.test.testcases.TestSuper2SetterGetter extends com.jpexs.flash.test.testcases.TestSuperSetterGetter {		
    public function testSuperGetSet() {
        super.myvar = 3;
        trace(super.myvar);
        super.myvar();
        new super.myvar();
        delete super.myvar;
        super.myvar++
        trace(super.myvar++);
        trace(++super.myvar);
    }
}