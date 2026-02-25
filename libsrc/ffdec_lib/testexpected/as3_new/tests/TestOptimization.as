package tests
{
   public class TestOptimization
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
      
      public function TestOptimization()
      {
         method
            name "tests:TestOptimization/TestOptimization"
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
               name "tests:TestOptimization/run"
               returns null
               
               body
                  maxstack 2
                  localcount 10
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "f", 0, 13
                     debug 1, "g", 1, 14
                     debug 1, "h", 2, 15
                     debug 1, "a", 3, 16
                     debug 1, "b", 4, 17
                     debug 1, "c", 5, 18
                     debug 1, "d", 6, 19
                     debug 1, "e", 7, 20
                     debug 1, "i", 8, 21
                     pushbyte 0
                     convert_i
                     setlocal1
                     pushbyte 0
                     convert_i
                     setlocal2
                     pushbyte 0
                     convert_i
                     setlocal3
                     pushbyte 1
                     convert_i
                     setlocal 4
                     pushbyte 2
                     convert_i
                     setlocal 5
                     pushbyte 3
                     convert_i
                     setlocal 6
                     pushbyte 4
                     convert_i
                     setlocal 7
                     getlocal 7
                     pushbyte 5
                     add
                     convert_i
                     setlocal 8
                     getlocal1
                     convert_i
                     dup
                     setlocal2
                     convert_i
                     dup
                     setlocal3
                     convert_i
                     setlocal 9
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
            findpropstrict Multiname("TestOptimization",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestOptimization")
            returnvoid
         end ; code
      end ; body
   end ; method
   
