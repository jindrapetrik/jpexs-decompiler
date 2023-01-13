package tests_classes
{
	import tests_other.myInternal;
    import tests_other.myInternal2;
    
	public class TestModifiers
	{
        private static var attr_stat_private:int = 1;
        public static var attr_stat_public:int = 2;
        internal static var attr_stat_internal:int = 3;
        protected static var attr_stat_protected:int = 4;
        myInternal static var attr_stat_namespace_explicit:int = 5;
        myInternal2 static var attr_stat_namespace_implicit:int = 6;
    
    
        private var attr_inst_private:int = 7;
        public var attr_inst_public:int = 8;
        internal var attr_inst_internal:int = 9;
        protected var attr_inst_protected:int = 10;
        myInternal var attr_inst_namespace_explicit:int = 11;
        myInternal2 var attr_inst_namespace_implicit:int = 12;
        
        
        
        private static function func_stat_private():int {
            return 1;
        }
        public static function func_stat_public():int {
            return 2;
        }
        internal static function func_stat_internal():int {
            return 3;
        }
        protected static function func_stat_protected():int {
            return 4;            
        }
        
        myInternal static function func_stat_namespace_explicit():int {
            return 5;            
        }
        
        myInternal2 static function func_stat_namespace_implicit():int {
            return 6;            
        }
        
        
        private function func_inst_private():int {
            return 7;
        }        
        public function func_inst_public():int {
            return 8;
        }        
        internal function func_inst_internal():int {
            return 9;
        }                
        protected function func_inst_protected():int {
            return 10;            
        }
        
        myInternal function func_inst_namespace_explicit():int {
            return 11;            
        }
        
        myInternal2 function func_inst_namespace_implicit():int {
            return 12;            
        }                                                
	}
}
