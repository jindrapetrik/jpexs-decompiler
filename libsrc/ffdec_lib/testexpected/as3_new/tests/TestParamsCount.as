package tests
{
   public class TestParamsCount
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
      
      public function TestParamsCount()
      {
         method
            name "tests:TestParamsCount/TestParamsCount"
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
               name "tests:TestParamsCount/run"
               param QName(PackageNamespace(""),"int")
               param QName(PackageNamespace(""),"int")
               param QName(PackageNamespace(""),"int")
               returns QName(PackageNamespace(""),"int")
               
               body
                  maxstack 1
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
            findpropstrict Multiname("TestParamsCount",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestParamsCount")
            returnvoid
         end ; code
      end ; body
   end ; method
   
