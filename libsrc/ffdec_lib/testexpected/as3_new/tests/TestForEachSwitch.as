package tests
{
   public class TestForEachSwitch
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
      
      public function TestForEachSwitch()
      {
         method
            name "tests:TestForEachSwitch/TestForEachSwitch"
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
               name "tests:TestForEachSwitch/run"
               returns null
               
               body
                  maxstack 2
                  localcount 10
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "name", 0, 13
                     debug 1, "a", 1, 14
                     debug 1, "b", 2, 15
                     debug 1, "c", 3, 16
                     debug 1, "s", 4, 17
                     debug 1, "obj", 5, 18
                     pushnull
                     coerce_s
                     setlocal1
                     pushtrue
                     convert_b
                     setlocal2
                     pushtrue
                     convert_b
                     setlocal3
                     pushtrue
                     convert_b
                     setlocal 4
                     pushbyte 5
                     convert_i
                     setlocal 5
                     newobject 0
                     coerce QName(PackageNamespace(""),"Object")
                     setlocal 6
                     pushbyte 0
                     setlocal 7
                     getlocal 6
                     coerce_a
                     setlocal 8
                     jump ofs00f4
            ofs0045:
                     label
                     getlocal 8
                     getlocal 7
                     nextvalue
                     coerce_s
                     setlocal1
                     getlocal2
                     iffalse ofs00ed
                     jump ofs009c
            ofs0056:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")])
                     pushstring "1"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")]), 1
                     getlocal3
                     iffalse ofs006a
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")])
                     pushstring "1b"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")]), 1
            ofs006a:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")])
                     pushstring "2"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")]), 1
                     getlocal 4
                     iffalse ofs007f
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")])
                     pushstring "2c"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")]), 1
            ofs007f:
                     jump ofs00ed
            ofs0083:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")])
                     pushstring "3"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")]), 1
                     jump ofs00ed
            ofs008f:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")])
                     pushstring "4"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")]), 1
            ofs0097:
                     label
                     jump ofs00ed
            ofs009c:
                     getlocal 5
                     setlocal 9
                     pushbyte 1
                     getlocal 9
                     ifstrictne ofs00ae
                     pushbyte 0
                     jump ofs00da
            ofs00ae:
                     pushbyte 2
                     getlocal 9
                     ifstrictne ofs00bc
                     pushbyte 1
                     jump ofs00da
            ofs00bc:
                     pushbyte 3
                     getlocal 9
                     ifstrictne ofs00ca
                     pushbyte 2
                     jump ofs00da
            ofs00ca:
                     pushbyte 4
                     getlocal 9
                     ifstrictne ofs00d8
                     pushbyte 3
                     jump ofs00da
            ofs00d8:
                     pushbyte -1
            ofs00da:
                     kill 9
                     lookupswitch ofs0097, [ofs0056, ofs006a, ofs0083, ofs008f]
            ofs00ed:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")])
                     pushstring "before_continue"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")]), 1
            ofs00f4:
                     hasnext2 8, 7
                     iftrue ofs0045
                     kill 8
                     kill 7
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
            findpropstrict Multiname("TestForEachSwitch",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestForEachSwitch")
            returnvoid
         end ; code
      end ; body
   end ; method
   
