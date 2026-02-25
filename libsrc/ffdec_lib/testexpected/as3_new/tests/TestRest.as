package tests
{
   public class TestRest
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
      
      public function TestRest()
      {
         method
            name "tests:TestRest/TestRest"
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
      
      public function run(firstp:int, ... restval) : int
      {
         trait method QName(PackageNamespace(""),"run")
            dispid 0
            method
               name "tests:TestRest/run"
               flag NEED_REST
               param QName(PackageNamespace(""),"int")
               returns QName(PackageNamespace(""),"int")
               
               body
                  maxstack 4
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "firstp", 0, 0
                     debug 1, "restval", 1, 0
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestRest"),ProtectedNamespace("tests:TestRest"),StaticProtectedNs("tests:TestRest"),PrivateNamespace("TestRest.as$0")])
                     pushstring "firstRest:"
                     getlocal2
                     pushbyte 0
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestRest"),ProtectedNamespace("tests:TestRest"),StaticProtectedNs("tests:TestRest"),PrivateNamespace("TestRest.as$0")])
                     add
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestRest"),ProtectedNamespace("tests:TestRest"),StaticProtectedNs("tests:TestRest"),PrivateNamespace("TestRest.as$0")]), 1
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
            findpropstrict Multiname("TestRest",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestRest")
            returnvoid
         end ; code
      end ; body
   end ; method
   
