package tests_classes
{
	
	public class TestModifiers
	{
        private var attr_inst_private:int = 1;
        public var attr_inst_public:int = 2;
        internal var attr_inst_internal:int = 3;
        protected var attr_inst_protected:int = 4;
        
        explicit var attr_exp:int = 9;
        
        private static var attr_stat_private:int = 5;
        public static var attr_stat_public:int = 6;
        internal static var attr_stat_internal:int = 7;
        protected static var attr_stat_protected:int = 8;
        
        private function func_inst_private():int {
            return 1;
        }        
        public function func_inst_public():int {
            return 2;
        }        
        internal function func_inst_internal():int {
            return 3;
        }                
        protected function func_inst_protected():int {
            return 4;            
        }
        
        private static function func_stat_private():int {
            return 5;
        }
        public static function func_stat_public():int {
            return 6;
        }
        internal static function func_stat_internal():int {
            return 7;
        }
        protected static function func_stat_protected():int {
            return 8;            
        }
        
        
        
        
	}
}
