package tests
{
   public class TestParamNames
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
      
      public function TestParamNames()
      {
         method
            name "tests:TestParamNames/TestParamNames"
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
      
      public function run(firstp:int, secondp:int, thirdp:int) : int
      {
         trait method QName(PackageNamespace(""),"run")
            dispid 0
            method
               name "tests:TestParamNames/run"
               param QName(PackageNamespace(""),"int")
               param QName(PackageNamespace(""),"int")
               param QName(PackageNamespace(""),"int")
               returns QName(PackageNamespace(""),"int")
               
               body
                  maxstack 2
                  localcount 4
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "firstp", 0, 0
                     debug 1, "secondp", 1, 0
                     debug 1, "thirdp", 2, 0
                     getlocal1
                     getlocal2
                     add
                     getlocal3
                     add
                     returnvalue
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
            findpropstrict Multiname("TestParamNames",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestParamNames")
            returnvoid
         end ; code
      end ; body
   end ; method
   
