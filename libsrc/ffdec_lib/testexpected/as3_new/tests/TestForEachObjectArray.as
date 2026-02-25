package tests
{
   public class TestForEachObjectArray
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
      
      public function TestForEachObjectArray()
      {
         method
            name "tests:TestForEachObjectArray/TestForEachObjectArray"
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
               name "tests:TestForEachObjectArray/run"
               returns null
               
               body
                  maxstack 4
                  localcount 5
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "list", 0, 13
                     debug 1, "test", 1, 14
                     pushnull
                     coerce QName(PackageNamespace(""),"Array")
                     setlocal1
                     pushnull
                     coerce QName(PackageNamespace(""),"Array")
                     setlocal2
                     findpropstrict QName(PackageNamespace(""),"Array")
                     constructprop QName(PackageNamespace(""),"Array"), 0
                     coerce QName(PackageNamespace(""),"Array")
                     setlocal1
                     getlocal1
                     pushbyte 0
                     pushstring "first"
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachObjectArray"),ProtectedNamespace("tests:TestForEachObjectArray"),StaticProtectedNs("tests:TestForEachObjectArray"),PrivateNamespace("TestForEachObjectArray.as$0")])
                     getlocal1
                     pushbyte 1
                     pushstring "second"
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachObjectArray"),ProtectedNamespace("tests:TestForEachObjectArray"),StaticProtectedNs("tests:TestForEachObjectArray"),PrivateNamespace("TestForEachObjectArray.as$0")])
                     getlocal1
                     pushbyte 2
                     pushstring "third"
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachObjectArray"),ProtectedNamespace("tests:TestForEachObjectArray"),StaticProtectedNs("tests:TestForEachObjectArray"),PrivateNamespace("TestForEachObjectArray.as$0")])
                     findpropstrict QName(PackageNamespace(""),"Array")
                     constructprop QName(PackageNamespace(""),"Array"), 0
                     coerce QName(PackageNamespace(""),"Array")
                     setlocal2
                     getlocal2
                     pushbyte 0
                     pushbyte 0
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachObjectArray"),ProtectedNamespace("tests:TestForEachObjectArray"),StaticProtectedNs("tests:TestForEachObjectArray"),PrivateNamespace("TestForEachObjectArray.as$0")])
                     pushbyte 0
                     setlocal3
                     getlocal1
                     coerce_a
                     setlocal 4
                     jump ofs0062
            ofs004b:
                     label
                     getlocal2
                     pushbyte 0
                     getlocal 4
                     getlocal3
                     nextvalue
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachObjectArray"),ProtectedNamespace("tests:TestForEachObjectArray"),StaticProtectedNs("tests:TestForEachObjectArray"),PrivateNamespace("TestForEachObjectArray.as$0")])
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachObjectArray"),ProtectedNamespace("tests:TestForEachObjectArray"),StaticProtectedNs("tests:TestForEachObjectArray"),PrivateNamespace("TestForEachObjectArray.as$0")])
                     pushstring "item #"
                     getlocal2
                     pushbyte 0
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachObjectArray"),ProtectedNamespace("tests:TestForEachObjectArray"),StaticProtectedNs("tests:TestForEachObjectArray"),PrivateNamespace("TestForEachObjectArray.as$0")])
                     add
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachObjectArray"),ProtectedNamespace("tests:TestForEachObjectArray"),StaticProtectedNs("tests:TestForEachObjectArray"),PrivateNamespace("TestForEachObjectArray.as$0")]), 1
            ofs0062:
                     hasnext2 4, 3
                     iftrue ofs004b
                     kill 4
                     kill 3
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
            findpropstrict Multiname("TestForEachObjectArray",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestForEachObjectArray")
            returnvoid
         end ; code
      end ; body
   end ; method
   
