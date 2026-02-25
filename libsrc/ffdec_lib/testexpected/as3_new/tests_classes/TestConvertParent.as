package tests_classes
{
   public class TestConvertParent
   {
      
      protected static var sprot:int = 6;
      
      method
         name ""
         returns null
         
         body
            maxstack 2
            localcount 1
            initscopedepth 3
            maxscopedepth 4
            
            code
               getlocal0
               pushscope
               findproperty QName(StaticProtectedNs("tests_classes:TestConvertParent"),"sprot")
               pushbyte 6
               setproperty QName(StaticProtectedNs("tests_classes:TestConvertParent"),"sprot")
               returnvoid
            end ; code
         end ; body
      end ; method
      
      protected var prot:int = 5;
      
      public function TestConvertParent()
      {
         method
            name "tests_classes:TestConvertParent/TestConvertParent"
            returns null
            
            body
               maxstack 1
               localcount 1
               initscopedepth 4
               maxscopedepth 5
               
               code
                  getlocal0
                  pushscope
                  getlocal0
                  constructsuper 0
                  returnvoid
               end ; code
            end ; body
         end ; method
      }
   }
}

method
   name ""
   returns null
   
   body
      maxstack 2
      localcount 1
      initscopedepth 1
      maxscopedepth 3
      
      code
         getlocal0
         pushscope
         findpropstrict Multiname("TestConvertParent",[PackageNamespace("tests_classes")])
         getlex QName(PackageNamespace(""),"Object")
         pushscope
         getlex QName(PackageNamespace(""),"Object")
         newclass 0
         popscope
         initproperty QName(PackageNamespace("tests_classes"),"TestConvertParent")
         returnvoid
      end ; code
   end ; body
end ; method

