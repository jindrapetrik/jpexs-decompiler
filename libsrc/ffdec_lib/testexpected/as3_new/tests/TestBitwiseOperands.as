package tests
{
   public class TestBitwiseOperands
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
      
      public function TestBitwiseOperands()
      {
         method
            name "tests:TestBitwiseOperands/TestBitwiseOperands"
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
               name "tests:TestBitwiseOperands/run"
               returns null
               
               body
                  maxstack 2
                  localcount 9
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
                     debug 1, "f", 5, 18
                     debug 1, "g", 6, 19
                     debug 1, "h", 7, 20
                     pushbyte 100
                     convert_i
                     setlocal1
                     getlocal1
                     pushshort 2303
                     bitand
                     convert_i
                     setlocal2
                     pushshort 2303
                     getlocal1
                     bitand
                     convert_i
                     setlocal3
                     getlocal1
                     pushshort 1152
                     bitor
                     convert_i
                     setlocal 4
                     pushshort 1152
                     getlocal1
                     bitor
                     convert_i
                     setlocal 5
                     getlocal1
                     pushshort 1601
                     bitxor
                     convert_i
                     setlocal 6
                     pushshort 1601
                     getlocal1
                     bitxor
                     convert_i
                     setlocal 7
                     pushshort 384
                     bitnot
                     convert_i
                     setlocal 8
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
            findpropstrict Multiname("TestBitwiseOperands",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestBitwiseOperands")
            returnvoid
         end ; code
      end ; body
   end ; method
   
