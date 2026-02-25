package tests_uses
{
   public interface TestParentInterface
   {
      
      method
         name ""
         returns null
         
         body
            maxstack 1
            localcount 1
            initscopedepth 2
            maxscopedepth 3
            
            code
               getlocal0
               pushscope
               returnvoid
            end ; code
         end ; body
      end ; method
      
      function parentInterfaceMethod() : void;
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
         findpropstrict Multiname("TestParentInterface",[PackageNamespace("tests_uses")])
         pushnull
         newclass 0
         initproperty QName(PackageNamespace("tests_uses"),"TestParentInterface")
         returnvoid
      end ; code
   end ; body
end ; method

