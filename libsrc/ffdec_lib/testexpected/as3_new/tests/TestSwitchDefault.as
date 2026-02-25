package tests
{
   public class TestSwitchDefault
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
      
      public function TestSwitchDefault()
      {
         method
            name "tests:TestSwitchDefault/TestSwitchDefault"
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
               name "tests:TestSwitchDefault/run"
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
                     jump ofs0048
            ofs000f:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchDefault"),ProtectedNamespace("tests:TestSwitchDefault"),StaticProtectedNs("tests:TestSwitchDefault"),PrivateNamespace("TestSwitchDefault.as$0")])
                     pushstring "fiftyseven multiply a"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchDefault"),ProtectedNamespace("tests:TestSwitchDefault"),StaticProtectedNs("tests:TestSwitchDefault"),PrivateNamespace("TestSwitchDefault.as$0")]), 1
                     jump ofs0095
            ofs001b:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchDefault"),ProtectedNamespace("tests:TestSwitchDefault"),StaticProtectedNs("tests:TestSwitchDefault"),PrivateNamespace("TestSwitchDefault.as$0")])
                     pushstring "thirteen"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchDefault"),ProtectedNamespace("tests:TestSwitchDefault"),StaticProtectedNs("tests:TestSwitchDefault"),PrivateNamespace("TestSwitchDefault.as$0")]), 1
            ofs0023:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchDefault"),ProtectedNamespace("tests:TestSwitchDefault"),StaticProtectedNs("tests:TestSwitchDefault"),PrivateNamespace("TestSwitchDefault.as$0")])
                     pushstring "fourteen"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchDefault"),ProtectedNamespace("tests:TestSwitchDefault"),StaticProtectedNs("tests:TestSwitchDefault"),PrivateNamespace("TestSwitchDefault.as$0")]), 1
                     jump ofs0095
            ofs002f:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchDefault"),ProtectedNamespace("tests:TestSwitchDefault"),StaticProtectedNs("tests:TestSwitchDefault"),PrivateNamespace("TestSwitchDefault.as$0")])
                     pushstring "eightynine"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchDefault"),ProtectedNamespace("tests:TestSwitchDefault"),StaticProtectedNs("tests:TestSwitchDefault"),PrivateNamespace("TestSwitchDefault.as$0")]), 1
                     jump ofs0095
            ofs003b:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchDefault"),ProtectedNamespace("tests:TestSwitchDefault"),StaticProtectedNs("tests:TestSwitchDefault"),PrivateNamespace("TestSwitchDefault.as$0")])
                     pushstring "default clause"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchDefault"),ProtectedNamespace("tests:TestSwitchDefault"),StaticProtectedNs("tests:TestSwitchDefault"),PrivateNamespace("TestSwitchDefault.as$0")]), 1
                     label
                     jump ofs0095
            ofs0048:
                     getlocal1
                     setlocal2
                     pushbyte 57
                     getlocal1
                     multiply
                     getlocal2
                     ifstrictne ofs0059
                     pushbyte 0
                     jump ofs0082
            ofs0059:
                     pushbyte 13
                     getlocal2
                     ifstrictne ofs0066
                     pushbyte 1
                     jump ofs0082
            ofs0066:
                     pushbyte 14
                     getlocal2
                     ifstrictne ofs0073
                     pushbyte 2
                     jump ofs0082
            ofs0073:
                     pushbyte 89
                     getlocal2
                     ifstrictne ofs0080
                     pushbyte 3
                     jump ofs0082
            ofs0080:
                     pushbyte -1
            ofs0082:
                     kill 2
                     lookupswitch ofs003b, [ofs000f, ofs001b, ofs0023, ofs002f]
            ofs0095:
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
            findpropstrict Multiname("TestSwitchDefault",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestSwitchDefault")
            returnvoid
         end ; code
      end ; body
   end ; method
   
