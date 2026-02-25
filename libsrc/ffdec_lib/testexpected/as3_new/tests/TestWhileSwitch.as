package tests
{
   public class TestWhileSwitch
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
      
      public function TestWhileSwitch()
      {
         method
            name "tests:TestWhileSwitch/TestWhileSwitch"
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
               name "tests:TestWhileSwitch/run"
               returns null
               
               body
                  maxstack 2
                  localcount 6
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     debug 1, "d", 1, 14
                     debug 1, "e", 2, 15
                     debug 1, "i", 3, 16
                     pushtrue
                     convert_b
                     setlocal1
                     pushbyte 5
                     convert_i
                     setlocal2
                     pushtrue
                     convert_b
                     setlocal3
                     pushbyte 0
                     convert_i
                     setlocal 4
                     jump ofs007d
            ofs0029:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileSwitch"),ProtectedNamespace("tests:TestWhileSwitch"),StaticProtectedNs("tests:TestWhileSwitch"),PrivateNamespace("TestWhileSwitch.as$0")])
                     pushstring "start"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileSwitch"),ProtectedNamespace("tests:TestWhileSwitch"),StaticProtectedNs("tests:TestWhileSwitch"),PrivateNamespace("TestWhileSwitch.as$0")]), 1
                     getlocal1
                     iffalse ofs0041
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileSwitch"),ProtectedNamespace("tests:TestWhileSwitch"),StaticProtectedNs("tests:TestWhileSwitch"),PrivateNamespace("TestWhileSwitch.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileSwitch"),ProtectedNamespace("tests:TestWhileSwitch"),StaticProtectedNs("tests:TestWhileSwitch"),PrivateNamespace("TestWhileSwitch.as$0")]), 1
                     jump ofs006f
            ofs0041:
                     jump ofs0052
            ofs0045:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileSwitch"),ProtectedNamespace("tests:TestWhileSwitch"),StaticProtectedNs("tests:TestWhileSwitch"),PrivateNamespace("TestWhileSwitch.as$0")])
                     pushstring "D1"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileSwitch"),ProtectedNamespace("tests:TestWhileSwitch"),StaticProtectedNs("tests:TestWhileSwitch"),PrivateNamespace("TestWhileSwitch.as$0")]), 1
            ofs004d:
                     label
                     jump ofs006f
            ofs0052:
                     getlocal2
                     setlocal 5
                     pushbyte 1
                     getlocal 5
                     ifstrictne ofs0063
                     pushbyte 0
                     jump ofs0065
            ofs0063:
                     pushbyte -1
            ofs0065:
                     kill 5
                     lookupswitch ofs004d, [ofs0045]
            ofs006f:
                     getlocal3
                     iffalse ofs007b
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileSwitch"),ProtectedNamespace("tests:TestWhileSwitch"),StaticProtectedNs("tests:TestWhileSwitch"),PrivateNamespace("TestWhileSwitch.as$0")])
                     pushstring "E"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileSwitch"),ProtectedNamespace("tests:TestWhileSwitch"),StaticProtectedNs("tests:TestWhileSwitch"),PrivateNamespace("TestWhileSwitch.as$0")]), 1
            ofs007b:
                     inclocal_i 4
            ofs007d:
                     getlocal 4
                     pushbyte 100
                     iflt ofs0029
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
            findpropstrict Multiname("TestWhileSwitch",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestWhileSwitch")
            returnvoid
         end ; code
      end ; body
   end ; method
   
