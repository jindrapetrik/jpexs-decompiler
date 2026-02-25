package tests_uses
{
   public class TestParentClass
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
      
      public var parentVar:int = 1;
      
      public function TestParentClass()
      {
         method
            name "tests_uses:TestParentClass/TestParentClass"
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
      
      public function parentClassMethod() : void
      {
         trait method QName(PackageNamespace(""),"parentClassMethod")
            dispid 0
            method
               name "tests_uses:TestParentClass/parentClassMethod"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 1
                  localcount 1
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
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
            findpropstrict Multiname("TestParentClass",[PackageNamespace("tests_uses")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests_uses"),"TestParentClass")
            returnvoid
         end ; code
      end ; body
   end ; method
   
