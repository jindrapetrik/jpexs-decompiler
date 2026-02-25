package tests
{
   public class TestCallLocal
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
      
      public function TestCallLocal()
      {
         method
            name "tests:TestCallLocal/TestCallLocal"
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
      
      public function getF() : Function
      {
         trait method QName(PackageNamespace(""),"getF")
            dispid 0
            method
               name "tests:TestCallLocal/getF"
               flag NEED_ACTIVATION
               returns QName(PackageNamespace(""),"Function")
               
               body
                  maxstack 2
                  localcount 2
                  initscopedepth 5
                  maxscopedepth 7
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "+$activation", 0, 0
                     newactivation
                     dup
                     setlocal1
                     pushscope
                     newfunction 3
                     returnvalue
                  end ; code
               end ; body
            end ; method
         }
         
         public function run() : *
         {
            trait method QName(PackageNamespace(""),"run")
               dispid 0
               method
                  name "tests:TestCallLocal/run"
                  returns null
                  
                  body
                     maxstack 4
                     localcount 3
                     initscopedepth 4
                     maxscopedepth 5
                     
                     code
                        getlocal0
                        pushscope
                        debug 1, "f", 0, 21
                        debug 1, "b", 1, 22
                        getlocal0
                        callproperty QName(PackageNamespace(""),"getF"), 0
                        coerce QName(PackageNamespace(""),"Function")
                        setlocal1
                        getlocal1
                        getglobalscope
                        pushbyte 1
                        pushbyte 3
                        call 2
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
               findpropstrict Multiname("TestCallLocal",[PackageNamespace("tests")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 0
               popscope
               initproperty QName(PackageNamespace("tests"),"TestCallLocal")
               returnvoid
            end ; code
         end ; body
      end ; method
      
