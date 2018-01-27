/**
 * Class for including other TestCases, it is called from frame 65
 */
class com.jpexs.flash.test.TestLoader {

	public function includeTests() {
		new com.jpexs.flash.test.testcases.TestSetterGetter();
		new com.jpexs.flash.test.testcases.TestCallSetterGetter();
		new com.jpexs.flash.test.testcases.TestVarsMethods();      
		new com.jpexs.flash.test.testcases.TestMaintainOrder();
	}
}