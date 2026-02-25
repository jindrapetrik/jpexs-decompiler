package tests
{
   public class TestInlineFunctions2
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
      
      public function TestInlineFunctions2()
      {
         method
            name "tests:TestInlineFunctions2/TestInlineFunctions2"
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
               name "tests:TestInlineFunctions2/run"
               flag NEED_ACTIVATION
               returns null
               
               body
                  maxstack 4
                  localcount 2
                  initscopedepth 5
                  maxscopedepth 8
                  trait slot QName(PackageInternalNs("tests"),"f")
                     slotid 1
                     type QName(PackageNamespace(""),"Function")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"g")
                     slotid 2
                     type QName(PackageNamespace(""),"Function")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"h")
                     slotid 3
                     type QName(PackageNamespace(""),"Function")
                  end ; trait
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "+$activation", 0, 0
                     newactivation
                     dup
                     setlocal1
                     pushscope
                     getscopeobject 1
                     newfunction 2
                     coerce QName(PackageNamespace(""),"Function")
                     setslot 1
                     getscopeobject 1
                     newfunction 3
                     coerce QName(PackageNamespace(""),"Function")
                     setslot 2
                     getscopeobject 1
                     newobject 0
                     pushwith
                     newfunction 4
                     dup
                     getscopeobject 2
                     swap
                     setproperty QName(PackageNamespace("tests"),"h2")
                     popscope
                     coerce QName(PackageNamespace(""),"Function")
                     setslot 3
                     newfunction 5
                     getglobalscope
                     pushbyte 1
                     call 1
                     pop
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
            findpropstrict Multiname("TestInlineFunctions2",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestInlineFunctions2")
            returnvoid
         end ; code
      end ; body
   end ; method
   
