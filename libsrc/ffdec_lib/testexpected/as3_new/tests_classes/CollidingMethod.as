package tests_classes
{
   public class CollidingMethod
   {
      
      method
         name ""
         returns null
         
         body
            maxstack 1
            localcount 1
            initscopedepth 3
            maxscopedepth 4
            
            code
               getlocal0
               pushscope
               returnvoid
            end ; code
         end ; body
      end ; method
      
      public function CollidingMethod()
      {
         method
            name "tests_classes:CollidingMethod/CollidingMethod"
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
         findpropstrict Multiname("CollidingMethod",[PackageNamespace("tests_classes")])
         getlex QName(PackageNamespace(""),"Object")
         pushscope
         getlex QName(PackageNamespace(""),"Object")
         newclass 0
         popscope
         initproperty QName(PackageNamespace("tests_classes"),"CollidingMethod")
         returnvoid
      end ; code
   end ; body
end ; method

