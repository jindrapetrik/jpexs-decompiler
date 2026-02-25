package tests_uses
{
   public interface TestInterface extends TestParentInterface
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
      
      function interfaceMethod() : void;
   }
}

method
   name ""
   returns null
   
   body
      maxstack 2
      localcount 1
      initscopedepth 1
      maxscopedepth 2
      
      code
         getlocal0
         pushscope
         findpropstrict Multiname("TestInterface",[PackageNamespace("tests_uses")])
         pushnull
         newclass 0
         initproperty QName(PackageNamespace("tests_uses"),"TestInterface")
         returnvoid
      end ; code
   end ; body
end ; method

