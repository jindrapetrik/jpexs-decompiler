package tests
{
   public class TestInnerFunctionScope
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
      
      public function TestInnerFunctionScope()
      {
         method
            name "tests:TestInnerFunctionScope/TestInnerFunctionScope"
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
      
      public function run(a:String) : *
      {
         trait method QName(PackageNamespace(""),"run")
            dispid 0
            method
               name "tests:TestInnerFunctionScope/run"
               flag NEED_ACTIVATION
               param QName(PackageNamespace(""),"String")
               returns null
               
               body
                  maxstack 2
                  localcount 3
                  initscopedepth 5
                  maxscopedepth 7
                  trait slot QName(PackageInternalNs("tests"),"a")
                     slotid 1
                     type QName(PackageNamespace(""),"String")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"innerFunc")
                     slotid 2
                     type QName(PackageNamespace(""),"Function")
                  end ; trait
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 0
                     debug 1, "+$activation", 1, 0
                     newactivation
                     dup
                     setlocal2
                     pushscope
                     getscopeobject 1
                     getlocal1
                     coerce_s
                     setslot 1
                     getscopeobject 1
                     newfunction 2
                     coerce QName(PackageNamespace(""),"Function")
                     setslot 2
                     findpropstrict Multiname("innerFunc",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerFunctionScope"),ProtectedNamespace("tests:TestInnerFunctionScope"),StaticProtectedNs("tests:TestInnerFunctionScope"),PrivateNamespace("TestInnerFunctionScope.as$0")])
                     getscopeobject 1
                     getslot 1
                     callpropvoid Multiname("innerFunc",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerFunctionScope"),ProtectedNamespace("tests:TestInnerFunctionScope"),StaticProtectedNs("tests:TestInnerFunctionScope"),PrivateNamespace("TestInnerFunctionScope.as$0")]), 1
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
      }
   }
   
   var testProm:int = 5;
   
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
            findpropstrict Multiname("TestInnerFunctionScope",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestInnerFunctionScope")
            findproperty QName(PrivateNamespace("TestInnerFunctionScope.as$0"),"testProm")
            pushbyte 5
            setproperty QName(PrivateNamespace("TestInnerFunctionScope.as$0"),"testProm")
            returnvoid
         end ; code
      end ; body
   end ; method
   
