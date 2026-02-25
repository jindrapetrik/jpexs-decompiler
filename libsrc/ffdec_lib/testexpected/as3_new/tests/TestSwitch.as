package tests
{
   public class TestSwitch
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
      
      public function TestSwitch()
      {
         method
            name "tests:TestSwitch/TestSwitch"
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
               name "tests:TestSwitch/run"
               returns null
               
               body
                  maxstack 2
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     pushbyte 5
                     coerce_a
                     setlocal1
                     jump ofs003c
            ofs000f:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitch"),ProtectedNamespace("tests:TestSwitch"),StaticProtectedNs("tests:TestSwitch"),PrivateNamespace("TestSwitch.as$0")])
                     pushstring "fiftyseven multiply a"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitch"),ProtectedNamespace("tests:TestSwitch"),StaticProtectedNs("tests:TestSwitch"),PrivateNamespace("TestSwitch.as$0")]), 1
                     jump ofs0089
            ofs001b:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitch"),ProtectedNamespace("tests:TestSwitch"),StaticProtectedNs("tests:TestSwitch"),PrivateNamespace("TestSwitch.as$0")])
                     pushstring "thirteen"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitch"),ProtectedNamespace("tests:TestSwitch"),StaticProtectedNs("tests:TestSwitch"),PrivateNamespace("TestSwitch.as$0")]), 1
            ofs0023:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitch"),ProtectedNamespace("tests:TestSwitch"),StaticProtectedNs("tests:TestSwitch"),PrivateNamespace("TestSwitch.as$0")])
                     pushstring "fourteen"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitch"),ProtectedNamespace("tests:TestSwitch"),StaticProtectedNs("tests:TestSwitch"),PrivateNamespace("TestSwitch.as$0")]), 1
                     jump ofs0089
            ofs002f:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitch"),ProtectedNamespace("tests:TestSwitch"),StaticProtectedNs("tests:TestSwitch"),PrivateNamespace("TestSwitch.as$0")])
                     pushstring "eightynine"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitch"),ProtectedNamespace("tests:TestSwitch"),StaticProtectedNs("tests:TestSwitch"),PrivateNamespace("TestSwitch.as$0")]), 1
            ofs0037:
                     label
                     jump ofs0089
            ofs003c:
                     getlocal1
                     setlocal2
                     pushbyte 57
                     getlocal1
                     multiply
                     getlocal2
                     ifstrictne ofs004d
                     pushbyte 0
                     jump ofs0076
            ofs004d:
                     pushbyte 13
                     getlocal2
                     ifstrictne ofs005a
                     pushbyte 1
                     jump ofs0076
            ofs005a:
                     pushbyte 14
                     getlocal2
                     ifstrictne ofs0067
                     pushbyte 2
                     jump ofs0076
            ofs0067:
                     pushbyte 89
                     getlocal2
                     ifstrictne ofs0074
                     pushbyte 3
                     jump ofs0076
            ofs0074:
                     pushbyte -1
            ofs0076:
                     kill 2
                     lookupswitch ofs0037, [ofs000f, ofs001b, ofs0023, ofs002f]
            ofs0089:
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
            findpropstrict Multiname("TestSwitch",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestSwitch")
            returnvoid
         end ; code
      end ; body
   end ; method
   
