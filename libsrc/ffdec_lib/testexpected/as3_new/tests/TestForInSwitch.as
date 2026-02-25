package tests
{
   public class TestForInSwitch
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
      
      public function TestForInSwitch()
      {
         method
            name "tests:TestForInSwitch/TestForInSwitch"
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
               name "tests:TestForInSwitch/run"
               returns null
               
               body
                  maxstack 3
                  localcount 6
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     debug 1, "arr", 1, 14
                     pushnull
                     coerce_s
                     setlocal1
                     pushstring "a"
                     pushstring "b"
                     pushstring "c"
                     newarray 3
                     coerce QName(PackageNamespace(""),"Array")
                     setlocal2
                     pushbyte 0
                     setlocal3
                     getlocal2
                     coerce_a
                     setlocal 4
                     jump ofs009b
            ofs0025:
                     label
                     getlocal 4
                     getlocal3
                     nextname
                     coerce_s
                     setlocal1
                     jump ofs0055
            ofs0030:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForInSwitch"),ProtectedNamespace("tests:TestForInSwitch"),StaticProtectedNs("tests:TestForInSwitch"),PrivateNamespace("TestForInSwitch.as$0")])
                     pushstring "val a"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForInSwitch"),ProtectedNamespace("tests:TestForInSwitch"),StaticProtectedNs("tests:TestForInSwitch"),PrivateNamespace("TestForInSwitch.as$0")]), 1
                     jump ofs0094
            ofs003c:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForInSwitch"),ProtectedNamespace("tests:TestForInSwitch"),StaticProtectedNs("tests:TestForInSwitch"),PrivateNamespace("TestForInSwitch.as$0")])
                     pushstring "val b"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForInSwitch"),ProtectedNamespace("tests:TestForInSwitch"),StaticProtectedNs("tests:TestForInSwitch"),PrivateNamespace("TestForInSwitch.as$0")]), 1
                     jump ofs0094
            ofs0048:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForInSwitch"),ProtectedNamespace("tests:TestForInSwitch"),StaticProtectedNs("tests:TestForInSwitch"),PrivateNamespace("TestForInSwitch.as$0")])
                     pushstring "val c"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForInSwitch"),ProtectedNamespace("tests:TestForInSwitch"),StaticProtectedNs("tests:TestForInSwitch"),PrivateNamespace("TestForInSwitch.as$0")]), 1
            ofs0050:
                     label
                     jump ofs0094
            ofs0055:
                     getlocal1
                     setlocal 5
                     pushstring "a"
                     getlocal 5
                     ifstrictne ofs0066
                     pushbyte 0
                     jump ofs0084
            ofs0066:
                     pushstring "b"
                     getlocal 5
                     ifstrictne ofs0074
                     pushbyte 1
                     jump ofs0084
            ofs0074:
                     pushstring "c"
                     getlocal 5
                     ifstrictne ofs0082
                     pushbyte 2
                     jump ofs0084
            ofs0082:
                     pushbyte -1
            ofs0084:
                     kill 5
                     lookupswitch ofs0050, [ofs0030, ofs003c, ofs0048]
            ofs0094:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForInSwitch"),ProtectedNamespace("tests:TestForInSwitch"),StaticProtectedNs("tests:TestForInSwitch"),PrivateNamespace("TestForInSwitch.as$0")])
                     pushstring "final"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForInSwitch"),ProtectedNamespace("tests:TestForInSwitch"),StaticProtectedNs("tests:TestForInSwitch"),PrivateNamespace("TestForInSwitch.as$0")]), 1
            ofs009b:
                     hasnext2 4, 3
                     iftrue ofs0025
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
            findpropstrict Multiname("TestForInSwitch",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestForInSwitch")
            returnvoid
         end ; code
      end ; body
   end ; method
   
