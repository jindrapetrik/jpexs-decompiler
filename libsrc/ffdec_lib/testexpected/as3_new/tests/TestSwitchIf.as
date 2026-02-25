package tests
{
   public class TestSwitchIf
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
      
      public function TestSwitchIf()
      {
         method
            name "tests:TestSwitchIf/TestSwitchIf"
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
               name "tests:TestSwitchIf/run"
               returns null
               
               body
                  maxstack 2
                  localcount 4
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "code", 0, 13
                     debug 1, "a", 1, 14
                     pushstring "4"
                     coerce_s
                     setlocal1
                     pushtrue
                     convert_b
                     setlocal2
                     jump ofs0029
            ofs0017:
                     label
                     getlocal2
                     iffalse ofs0024
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchIf"),ProtectedNamespace("tests:TestSwitchIf"),StaticProtectedNs("tests:TestSwitchIf"),PrivateNamespace("TestSwitchIf.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchIf"),ProtectedNamespace("tests:TestSwitchIf"),StaticProtectedNs("tests:TestSwitchIf"),PrivateNamespace("TestSwitchIf.as$0")]), 1
            ofs0024:
                     label
                     jump ofs0058
            ofs0029:
                     getlocal1
                     convert_i
                     pushbyte 2
                     subtract
                     setlocal3
                     pushbyte 0
                     getlocal3
                     ifstrictne ofs003c
                     pushbyte 0
                     jump ofs004b
            ofs003c:
                     pushbyte 1
                     getlocal3
                     ifstrictne ofs0049
                     pushbyte 1
                     jump ofs004b
            ofs0049:
                     pushbyte -1
            ofs004b:
                     kill 3
                     lookupswitch ofs0024, [ofs0017, ofs0017]
            ofs0058:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchIf"),ProtectedNamespace("tests:TestSwitchIf"),StaticProtectedNs("tests:TestSwitchIf"),PrivateNamespace("TestSwitchIf.as$0")])
                     pushstring "B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchIf"),ProtectedNamespace("tests:TestSwitchIf"),StaticProtectedNs("tests:TestSwitchIf"),PrivateNamespace("TestSwitchIf.as$0")]), 1
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
            findpropstrict Multiname("TestSwitchIf",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestSwitchIf")
            returnvoid
         end ; code
      end ; body
   end ; method
   
