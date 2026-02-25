package tests
{
   public class TestForEach
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
      
      public function TestForEach()
      {
         method
            name "tests:TestForEach/TestForEach"
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
               name "tests:TestForEach/run"
               returns null
               
               body
                  maxstack 3
                  localcount 5
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "list", 0, 13
                     debug 1, "item", 1, 14
                     pushnull
                     coerce QName(PackageNamespace(""),"Array")
                     setlocal1
                     pushundefined
                     coerce_a
                     setlocal2
                     findpropstrict QName(PackageNamespace(""),"Array")
                     constructprop QName(PackageNamespace(""),"Array"), 0
                     coerce QName(PackageNamespace(""),"Array")
                     setlocal1
                     getlocal1
                     pushbyte 0
                     pushstring "first"
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEach"),ProtectedNamespace("tests:TestForEach"),StaticProtectedNs("tests:TestForEach"),PrivateNamespace("TestForEach.as$0")])
                     getlocal1
                     pushbyte 1
                     pushstring "second"
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEach"),ProtectedNamespace("tests:TestForEach"),StaticProtectedNs("tests:TestForEach"),PrivateNamespace("TestForEach.as$0")])
                     getlocal1
                     pushbyte 2
                     pushstring "third"
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEach"),ProtectedNamespace("tests:TestForEach"),StaticProtectedNs("tests:TestForEach"),PrivateNamespace("TestForEach.as$0")])
                     pushbyte 0
                     setlocal3
                     getlocal1
                     coerce_a
                     setlocal 4
                     jump ofs004b
            ofs003b:
                     label
                     getlocal 4
                     getlocal3
                     nextvalue
                     coerce_a
                     setlocal2
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEach"),ProtectedNamespace("tests:TestForEach"),StaticProtectedNs("tests:TestForEach"),PrivateNamespace("TestForEach.as$0")])
                     pushstring "item #"
                     getlocal2
                     add
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEach"),ProtectedNamespace("tests:TestForEach"),StaticProtectedNs("tests:TestForEach"),PrivateNamespace("TestForEach.as$0")]), 1
            ofs004b:
                     hasnext2 4, 3
                     iftrue ofs003b
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
            findpropstrict Multiname("TestForEach",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestForEach")
            returnvoid
         end ; code
      end ; body
   end ; method
   
