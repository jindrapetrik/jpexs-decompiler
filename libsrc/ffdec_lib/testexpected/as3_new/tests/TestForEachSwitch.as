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
                     jump ofs008f
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
                     jump ofs00e0
            ofs0076:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")])
                     pushstring "3"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")]), 1
                     jump ofs00e0
            ofs0082:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")])
                     pushstring "4"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")]), 1
            ofs008a:
                     label
                     jump ofs00e0
            ofs008f:
                     getlocal 5
                     setlocal 9
                     pushbyte 1
                     getlocal 9
                     ifstrictne ofs00a1
                     pushbyte 0
                     jump ofs00cd
            ofs00a1:
                     pushbyte 2
                     getlocal 9
                     ifstrictne ofs00af
                     pushbyte 1
                     jump ofs00cd
            ofs00af:
                     pushbyte 3
                     getlocal 9
                     ifstrictne ofs00bd
                     pushbyte 2
                     jump ofs00cd
            ofs00bd:
                     pushbyte 4
                     getlocal 9
                     ifstrictne ofs00cb
                     pushbyte 3
                     jump ofs00cd
            ofs00cb:
                     pushbyte -1
            ofs00cd:
                     kill 9
                     lookupswitch ofs008a, [ofs0056, ofs006a, ofs0076, ofs0082]
            ofs00e0:
                     getlocal 4
                     iffalse ofs00ed
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")])
                     pushstring "2c"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachSwitch"),ProtectedNamespace("tests:TestForEachSwitch"),StaticProtectedNs("tests:TestForEachSwitch"),PrivateNamespace("TestForEachSwitch.as$0")]), 1
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
   
