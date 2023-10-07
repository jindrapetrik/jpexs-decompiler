import com.jpexs.flash.test.testcases.TestUninitializedFields;
		
class com.jpexs.flash.test.testcases.TestUninitializedFieldsUsage {
	        
        public function test() 
		{
            var v = new TestUninitializedFields();
            trace(v.a);
			trace(v.c);
            trace(TestUninitializedFields.b);
        }
		
}
