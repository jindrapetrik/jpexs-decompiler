package tests
{
   public class TestPrecedenceX
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
      
      public function TestPrecedenceX()
      {
         method
            name "tests:TestPrecedenceX/TestPrecedenceX"
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
      
      public function run() : *
      {
         trait method QName(PackageNamespace(""),"run")
            dispid 0
            method
               name "tests:TestPrecedenceX/run"
               returns null
               
               body
                  maxstack 3
                  localcount 6
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     debug 1, "b", 1, 14
                     debug 1, "c", 2, 15
                     debug 1, "d", 3, 16
                     debug 1, "e", 4, 17
                     pushbyte 5
                     coerce_a
                     setlocal1
                     pushbyte 2
                     coerce_a
                     setlocal2
                     pushbyte 3
                     coerce_a
                     setlocal3
                     getlocal1
                     getlocal2
                     getlocal3
                     urshift
                     lshift
                     coerce_a
                     setlocal 4
                     getlocal1
                     getlocal2
                     lshift
                     getlocal3
                     urshift
                     coerce_a
                     setlocal 5
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
            findpropstrict Multiname("TestPrecedenceX",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestPrecedenceX")
            returnvoid
         end ; code
      end ; body
   end ; method
   
