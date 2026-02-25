package tests
{
   public class TestInlineFunctions
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
      
      public function TestInlineFunctions()
      {
         method
            name "tests:TestInlineFunctions/TestInlineFunctions"
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
               name "tests:TestInlineFunctions/run"
               flag NEED_ACTIVATION
               returns null
               
               body
                  maxstack 2
                  localcount 2
                  initscopedepth 5
                  maxscopedepth 7
                  trait slot QName(PackageInternalNs("tests"),"first")
                     slotid 1
                     type QName(PackageNamespace(""),"String")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"traceParameter")
                     slotid 2
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
                     pushnull
                     coerce_s
                     setslot 1
                     getscopeobject 1
                     pushstring "value1"
                     coerce_s
                     setslot 1
                     getscopeobject 1
                     newfunction 3
                     coerce QName(PackageNamespace(""),"Function")
                     setslot 2
                     findpropstrict Multiname("traceParameter",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInlineFunctions"),ProtectedNamespace("tests:TestInlineFunctions"),StaticProtectedNs("tests:TestInlineFunctions"),PrivateNamespace("TestInlineFunctions.as$0")])
                     pushstring "hello"
                     callpropvoid Multiname("traceParameter",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInlineFunctions"),ProtectedNamespace("tests:TestInlineFunctions"),StaticProtectedNs("tests:TestInlineFunctions"),PrivateNamespace("TestInlineFunctions.as$0")]), 1
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
            findpropstrict Multiname("TestInlineFunctions",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestInlineFunctions")
            returnvoid
         end ; code
      end ; body
   end ; method
   
