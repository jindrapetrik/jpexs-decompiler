package tests
{
   public class TestDefaultNotLastGrouped
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
      
      public function TestDefaultNotLastGrouped()
      {
         method
            name "tests:TestDefaultNotLastGrouped/TestDefaultNotLastGrouped"
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
               name "tests:TestDefaultNotLastGrouped/run"
               returns null
               
               body
                  maxstack 2
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "k", 0, 13
                     pushbyte 10
                     coerce_a
                     setlocal1
                     jump ofs0030
            ofs000f:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDefaultNotLastGrouped"),ProtectedNamespace("tests:TestDefaultNotLastGrouped"),StaticProtectedNs("tests:TestDefaultNotLastGrouped"),PrivateNamespace("TestDefaultNotLastGrouped.as$0")])
                     pushstring "def and 6"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDefaultNotLastGrouped"),ProtectedNamespace("tests:TestDefaultNotLastGrouped"),StaticProtectedNs("tests:TestDefaultNotLastGrouped"),PrivateNamespace("TestDefaultNotLastGrouped.as$0")]), 1
            ofs0017:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDefaultNotLastGrouped"),ProtectedNamespace("tests:TestDefaultNotLastGrouped"),StaticProtectedNs("tests:TestDefaultNotLastGrouped"),PrivateNamespace("TestDefaultNotLastGrouped.as$0")])
                     pushstring "def and 6 and 5"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDefaultNotLastGrouped"),ProtectedNamespace("tests:TestDefaultNotLastGrouped"),StaticProtectedNs("tests:TestDefaultNotLastGrouped"),PrivateNamespace("TestDefaultNotLastGrouped.as$0")]), 1
                     jump ofs006b
            ofs0023:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDefaultNotLastGrouped"),ProtectedNamespace("tests:TestDefaultNotLastGrouped"),StaticProtectedNs("tests:TestDefaultNotLastGrouped"),PrivateNamespace("TestDefaultNotLastGrouped.as$0")])
                     pushstring "4"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDefaultNotLastGrouped"),ProtectedNamespace("tests:TestDefaultNotLastGrouped"),StaticProtectedNs("tests:TestDefaultNotLastGrouped"),PrivateNamespace("TestDefaultNotLastGrouped.as$0")]), 1
                     label
                     jump ofs006b
            ofs0030:
                     getlocal1
                     setlocal2
                     pushstring "six"
                     getlocal2
                     ifstrictne ofs003f
                     pushbyte 0
                     jump ofs005b
            ofs003f:
                     pushstring "five"
                     getlocal2
                     ifstrictne ofs004c
                     pushbyte 1
                     jump ofs005b
            ofs004c:
                     pushstring "four"
                     getlocal2
                     ifstrictne ofs0059
                     pushbyte 2
                     jump ofs005b
            ofs0059:
                     pushbyte -1
            ofs005b:
                     kill 2
                     lookupswitch ofs000f, [ofs000f, ofs0017, ofs0023]
            ofs006b:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDefaultNotLastGrouped"),ProtectedNamespace("tests:TestDefaultNotLastGrouped"),StaticProtectedNs("tests:TestDefaultNotLastGrouped"),PrivateNamespace("TestDefaultNotLastGrouped.as$0")])
                     pushstring "after switch"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDefaultNotLastGrouped"),ProtectedNamespace("tests:TestDefaultNotLastGrouped"),StaticProtectedNs("tests:TestDefaultNotLastGrouped"),PrivateNamespace("TestDefaultNotLastGrouped.as$0")]), 1
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
            findpropstrict Multiname("TestDefaultNotLastGrouped",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestDefaultNotLastGrouped")
            returnvoid
         end ; code
      end ; body
   end ; method
   
