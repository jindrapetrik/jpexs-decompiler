package tests
{
   public class TestIncDec7
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
      
      public function TestIncDec7()
      {
         method
            name "tests:TestIncDec7/TestIncDec7"
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
               name "tests:TestIncDec7/run"
               returns null
               
               body
                  maxstack 5
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     debug 1, "index", 1, 14
                     pushbyte 1
                     pushbyte 2
                     pushbyte 3
                     pushbyte 4
                     pushbyte 5
                     newarray 5
                     coerce_a
                     setlocal1
                     pushbyte 0
                     convert_i
                     setlocal2
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec7"),ProtectedNamespace("tests:TestIncDec7"),StaticProtectedNs("tests:TestIncDec7"),PrivateNamespace("TestIncDec7.as$0")])
                     pushstring "a[++index]"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec7"),ProtectedNamespace("tests:TestIncDec7"),StaticProtectedNs("tests:TestIncDec7"),PrivateNamespace("TestIncDec7.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec7"),ProtectedNamespace("tests:TestIncDec7"),StaticProtectedNs("tests:TestIncDec7"),PrivateNamespace("TestIncDec7.as$0")])
                     getlocal1
                     getlocal2
                     increment_i
                     dup
                     convert_i
                     setlocal2
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec7"),ProtectedNamespace("tests:TestIncDec7"),StaticProtectedNs("tests:TestIncDec7"),PrivateNamespace("TestIncDec7.as$0")])
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec7"),ProtectedNamespace("tests:TestIncDec7"),StaticProtectedNs("tests:TestIncDec7"),PrivateNamespace("TestIncDec7.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec7"),ProtectedNamespace("tests:TestIncDec7"),StaticProtectedNs("tests:TestIncDec7"),PrivateNamespace("TestIncDec7.as$0")])
                     pushstring "a[--index]"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec7"),ProtectedNamespace("tests:TestIncDec7"),StaticProtectedNs("tests:TestIncDec7"),PrivateNamespace("TestIncDec7.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec7"),ProtectedNamespace("tests:TestIncDec7"),StaticProtectedNs("tests:TestIncDec7"),PrivateNamespace("TestIncDec7.as$0")])
                     getlocal1
                     getlocal2
                     decrement_i
                     dup
                     convert_i
                     setlocal2
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec7"),ProtectedNamespace("tests:TestIncDec7"),StaticProtectedNs("tests:TestIncDec7"),PrivateNamespace("TestIncDec7.as$0")])
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec7"),ProtectedNamespace("tests:TestIncDec7"),StaticProtectedNs("tests:TestIncDec7"),PrivateNamespace("TestIncDec7.as$0")]), 1
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
            findpropstrict Multiname("TestIncDec7",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestIncDec7")
            returnvoid
         end ; code
      end ; body
   end ; method
   
