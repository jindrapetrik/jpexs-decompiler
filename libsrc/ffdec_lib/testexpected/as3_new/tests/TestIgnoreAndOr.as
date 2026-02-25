package tests
{
   public class TestIgnoreAndOr
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
      
      public function TestIgnoreAndOr()
      {
         method
            name "tests:TestIgnoreAndOr/TestIgnoreAndOr"
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
               name "tests:TestIgnoreAndOr/run"
               returns null
               
               body
                  maxstack 2
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "k", 0, 13
                     getlex QName(PackageNamespace(""),"Math")
                     callproperty QName(PackageNamespace(""),"random"), 0
                     convert_i
                     setlocal1
                     getlocal1
                     pushbyte 5
                     ifngt ofs001c
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIgnoreAndOr"),ProtectedNamespace("tests:TestIgnoreAndOr"),StaticProtectedNs("tests:TestIgnoreAndOr"),PrivateNamespace("TestIgnoreAndOr.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIgnoreAndOr"),ProtectedNamespace("tests:TestIgnoreAndOr"),StaticProtectedNs("tests:TestIgnoreAndOr"),PrivateNamespace("TestIgnoreAndOr.as$0")]), 1
            ofs001c:
                     getlocal1
                     pushbyte 10
                     ifngt ofs002a
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIgnoreAndOr"),ProtectedNamespace("tests:TestIgnoreAndOr"),StaticProtectedNs("tests:TestIgnoreAndOr"),PrivateNamespace("TestIgnoreAndOr.as$0")])
                     pushstring "B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIgnoreAndOr"),ProtectedNamespace("tests:TestIgnoreAndOr"),StaticProtectedNs("tests:TestIgnoreAndOr"),PrivateNamespace("TestIgnoreAndOr.as$0")]), 1
            ofs002a:
                     getlocal1
                     pushbyte 15
                     ifngt ofs0038
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIgnoreAndOr"),ProtectedNamespace("tests:TestIgnoreAndOr"),StaticProtectedNs("tests:TestIgnoreAndOr"),PrivateNamespace("TestIgnoreAndOr.as$0")])
                     pushstring "C"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIgnoreAndOr"),ProtectedNamespace("tests:TestIgnoreAndOr"),StaticProtectedNs("tests:TestIgnoreAndOr"),PrivateNamespace("TestIgnoreAndOr.as$0")]), 1
            ofs0038:
                     getlocal1
                     pushbyte 20
                     ifngt ofs0046
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIgnoreAndOr"),ProtectedNamespace("tests:TestIgnoreAndOr"),StaticProtectedNs("tests:TestIgnoreAndOr"),PrivateNamespace("TestIgnoreAndOr.as$0")])
                     pushstring "D"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIgnoreAndOr"),ProtectedNamespace("tests:TestIgnoreAndOr"),StaticProtectedNs("tests:TestIgnoreAndOr"),PrivateNamespace("TestIgnoreAndOr.as$0")]), 1
            ofs0046:
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
            findpropstrict Multiname("TestIgnoreAndOr",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestIgnoreAndOr")
            returnvoid
         end ; code
      end ; body
   end ; method
   
