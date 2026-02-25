package tests
{
   public class TestSwitchContinue
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
      
      public function TestSwitchContinue()
      {
         method
            name "tests:TestSwitchContinue/TestSwitchContinue"
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
               name "tests:TestSwitchContinue/run"
               returns null
               
               body
                  maxstack 2
                  localcount 4
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "i", 0, 13
                     debug 1, "r", 1, 14
                     pushbyte 0
                     convert_i
                     setlocal1
                     getlex QName(PackageNamespace(""),"Math")
                     callproperty QName(PackageNamespace(""),"random"), 0
                     pushbyte 10
                     modulo
                     convert_i
                     setlocal2
                     getlocal2
                     pushbyte 5
                     ifngt ofs00a7
                     pushbyte 0
                     convert_i
                     setlocal1
                     jump ofs00a0
            ofs0029:
                     label
                     jump ofs005c
            ofs002e:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchContinue"),ProtectedNamespace("tests:TestSwitchContinue"),StaticProtectedNs("tests:TestSwitchContinue"),PrivateNamespace("TestSwitchContinue.as$0")])
                     pushstring "hello"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchContinue"),ProtectedNamespace("tests:TestSwitchContinue"),StaticProtectedNs("tests:TestSwitchContinue"),PrivateNamespace("TestSwitchContinue.as$0")]), 1
                     jump ofs0097
            ofs003a:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchContinue"),ProtectedNamespace("tests:TestSwitchContinue"),StaticProtectedNs("tests:TestSwitchContinue"),PrivateNamespace("TestSwitchContinue.as$0")])
                     pushstring "hi"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchContinue"),ProtectedNamespace("tests:TestSwitchContinue"),StaticProtectedNs("tests:TestSwitchContinue"),PrivateNamespace("TestSwitchContinue.as$0")]), 1
                     jump ofs0097
            ofs0046:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchContinue"),ProtectedNamespace("tests:TestSwitchContinue"),StaticProtectedNs("tests:TestSwitchContinue"),PrivateNamespace("TestSwitchContinue.as$0")])
                     pushstring "howdy"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchContinue"),ProtectedNamespace("tests:TestSwitchContinue"),StaticProtectedNs("tests:TestSwitchContinue"),PrivateNamespace("TestSwitchContinue.as$0")]), 1
                     jump ofs0097
            ofs0052:
                     label
                     jump ofs009e
                     label
                     jump ofs0097
            ofs005c:
                     getlocal1
                     setlocal3
                     pushbyte 0
                     getlocal3
                     ifstrictne ofs006b
                     pushbyte 0
                     jump ofs0087
            ofs006b:
                     pushbyte 1
                     getlocal3
                     ifstrictne ofs0078
                     pushbyte 1
                     jump ofs0087
            ofs0078:
                     pushbyte 2
                     getlocal3
                     ifstrictne ofs0085
                     pushbyte 2
                     jump ofs0087
            ofs0085:
                     pushbyte -1
            ofs0087:
                     kill 3
                     lookupswitch ofs0052, [ofs002e, ofs003a, ofs0046]
            ofs0097:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchContinue"),ProtectedNamespace("tests:TestSwitchContinue"),StaticProtectedNs("tests:TestSwitchContinue"),PrivateNamespace("TestSwitchContinue.as$0")])
                     pushstring "message shown"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchContinue"),ProtectedNamespace("tests:TestSwitchContinue"),StaticProtectedNs("tests:TestSwitchContinue"),PrivateNamespace("TestSwitchContinue.as$0")]), 1
            ofs009e:
                     inclocal_i 1
            ofs00a0:
                     getlocal1
                     pushbyte 10
                     iflt ofs0029
            ofs00a7:
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
            findpropstrict Multiname("TestSwitchContinue",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestSwitchContinue")
            returnvoid
         end ; code
      end ; body
   end ; method
   
