package tests
{
   public class TestComplexExpressions
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
      
      public function TestComplexExpressions()
      {
         method
            name "tests:TestComplexExpressions/TestComplexExpressions"
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
               name "tests:TestComplexExpressions/run"
               returns null
               
               body
                  maxstack 4
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "i", 0, 13
                     debug 1, "j", 1, 14
                     pushbyte 0
                     convert_i
                     setlocal1
                     pushbyte 0
                     convert_i
                     setlocal2
                     getlocal1
                     getlocal1
                     getlocal1
                     dup
                     increment_i
                     convert_i
                     setlocal1
                     add
                     convert_i
                     dup
                     setlocal1
                     add
                     convert_i
                     dup
                     setlocal1
                     convert_i
                     setlocal2
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
            findpropstrict Multiname("TestComplexExpressions",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestComplexExpressions")
            returnvoid
         end ; code
      end ; body
   end ; method
   
