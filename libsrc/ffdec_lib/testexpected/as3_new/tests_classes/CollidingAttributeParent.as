package tests_classes
{
   public class CollidingAttributeParent
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
      
      public var CollidingAttribute2:int = 5;
      
      public function CollidingAttributeParent()
      {
         method
            name "tests_classes:CollidingAttributeParent/CollidingAttributeParent"
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
         findpropstrict Multiname("CollidingAttributeParent",[PackageNamespace("tests_classes")])
         getlex QName(PackageNamespace(""),"Object")
         pushscope
         getlex QName(PackageNamespace(""),"Object")
         newclass 0
         popscope
         initproperty QName(PackageNamespace("tests_classes"),"CollidingAttributeParent")
         returnvoid
      end ; code
   end ; body
end ; method

