package tests
{
   public class TestForEachObjectAttribute
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
      
      private var testPriv:int = 5;
      
      public function TestForEachObjectAttribute()
      {
         method
            name "tests:TestForEachObjectAttribute/TestForEachObjectAttribute"
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
               name "tests:TestForEachObjectAttribute/run"
               returns null
               
               body
                  maxstack 3
                  localcount 4
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "list", 0, 15
                     pushnull
                     coerce QName(PackageNamespace(""),"Array")
                     setlocal1
                     findpropstrict QName(PackageNamespace(""),"Array")
                     constructprop QName(PackageNamespace(""),"Array"), 0
                     coerce QName(PackageNamespace(""),"Array")
                     setlocal1
                     getlocal1
                     pushbyte 0
                     pushstring "first"
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachObjectAttribute"),ProtectedNamespace("tests:TestForEachObjectAttribute"),StaticProtectedNs("tests:TestForEachObjectAttribute"),PrivateNamespace("TestForEachObjectAttribute.as$0")])
                     getlocal1
                     pushbyte 1
                     pushstring "second"
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachObjectAttribute"),ProtectedNamespace("tests:TestForEachObjectAttribute"),StaticProtectedNs("tests:TestForEachObjectAttribute"),PrivateNamespace("TestForEachObjectAttribute.as$0")])
                     getlocal1
                     pushbyte 2
                     pushstring "third"
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachObjectAttribute"),ProtectedNamespace("tests:TestForEachObjectAttribute"),StaticProtectedNs("tests:TestForEachObjectAttribute"),PrivateNamespace("TestForEachObjectAttribute.as$0")])
                     pushbyte 0
                     setlocal2
                     getlocal1
                     coerce_a
                     setlocal3
                     jump ofs0044
            ofs0032:
                     label
                     getlocal0
                     getlocal3
                     getlocal2
                     nextvalue
                     setproperty QName(PrivateNamespace("tests:TestForEachObjectAttribute"),"testPriv")
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachObjectAttribute"),ProtectedNamespace("tests:TestForEachObjectAttribute"),StaticProtectedNs("tests:TestForEachObjectAttribute"),PrivateNamespace("TestForEachObjectAttribute.as$0")])
                     pushstring "item #"
                     getlocal0
                     getproperty QName(PrivateNamespace("tests:TestForEachObjectAttribute"),"testPriv")
                     add
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachObjectAttribute"),ProtectedNamespace("tests:TestForEachObjectAttribute"),StaticProtectedNs("tests:TestForEachObjectAttribute"),PrivateNamespace("TestForEachObjectAttribute.as$0")]), 1
            ofs0044:
                     hasnext2 3, 2
                     iftrue ofs0032
                     kill 3
                     kill 2
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
            findpropstrict Multiname("TestForEachObjectAttribute",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestForEachObjectAttribute")
            returnvoid
         end ; code
      end ; body
   end ; method
   
