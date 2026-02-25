package tests
{
   public class TestSwitchBig
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
      
      public function TestSwitchBig()
      {
         method
            name "tests:TestSwitchBig/TestSwitchBig"
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
               name "tests:TestSwitchBig/run"
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
                     jump ofs0054
            ofs000f:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchBig"),ProtectedNamespace("tests:TestSwitchBig"),StaticProtectedNs("tests:TestSwitchBig"),PrivateNamespace("TestSwitchBig.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchBig"),ProtectedNamespace("tests:TestSwitchBig"),StaticProtectedNs("tests:TestSwitchBig"),PrivateNamespace("TestSwitchBig.as$0")]), 1
                     jump ofs00df
            ofs001b:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchBig"),ProtectedNamespace("tests:TestSwitchBig"),StaticProtectedNs("tests:TestSwitchBig"),PrivateNamespace("TestSwitchBig.as$0")])
                     pushstring "BC"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchBig"),ProtectedNamespace("tests:TestSwitchBig"),StaticProtectedNs("tests:TestSwitchBig"),PrivateNamespace("TestSwitchBig.as$0")]), 1
                     jump ofs00df
            ofs0027:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchBig"),ProtectedNamespace("tests:TestSwitchBig"),StaticProtectedNs("tests:TestSwitchBig"),PrivateNamespace("TestSwitchBig.as$0")])
                     pushstring "D-default-E"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchBig"),ProtectedNamespace("tests:TestSwitchBig"),StaticProtectedNs("tests:TestSwitchBig"),PrivateNamespace("TestSwitchBig.as$0")]), 1
                     jump ofs00df
            ofs0033:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchBig"),ProtectedNamespace("tests:TestSwitchBig"),StaticProtectedNs("tests:TestSwitchBig"),PrivateNamespace("TestSwitchBig.as$0")])
                     pushstring "F no break"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchBig"),ProtectedNamespace("tests:TestSwitchBig"),StaticProtectedNs("tests:TestSwitchBig"),PrivateNamespace("TestSwitchBig.as$0")]), 1
            ofs003b:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchBig"),ProtectedNamespace("tests:TestSwitchBig"),StaticProtectedNs("tests:TestSwitchBig"),PrivateNamespace("TestSwitchBig.as$0")])
                     pushstring "G"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchBig"),ProtectedNamespace("tests:TestSwitchBig"),StaticProtectedNs("tests:TestSwitchBig"),PrivateNamespace("TestSwitchBig.as$0")]), 1
                     jump ofs00df
            ofs0047:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchBig"),ProtectedNamespace("tests:TestSwitchBig"),StaticProtectedNs("tests:TestSwitchBig"),PrivateNamespace("TestSwitchBig.as$0")])
                     pushstring "H last"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchBig"),ProtectedNamespace("tests:TestSwitchBig"),StaticProtectedNs("tests:TestSwitchBig"),PrivateNamespace("TestSwitchBig.as$0")]), 1
                     label
                     jump ofs00df
            ofs0054:
                     getlocal1
                     setlocal2
                     pushstring "A"
                     getlocal2
                     ifstrictne ofs0063
                     pushbyte 0
                     jump ofs00c0
            ofs0063:
                     pushstring "B"
                     getlocal2
                     ifstrictne ofs0070
                     pushbyte 1
                     jump ofs00c0
            ofs0070:
                     pushstring "C"
                     getlocal2
                     ifstrictne ofs007d
                     pushbyte 2
                     jump ofs00c0
            ofs007d:
                     pushstring "D"
                     getlocal2
                     ifstrictne ofs008a
                     pushbyte 3
                     jump ofs00c0
            ofs008a:
                     pushstring "E"
                     getlocal2
                     ifstrictne ofs0097
                     pushbyte 4
                     jump ofs00c0
            ofs0097:
                     pushstring "F"
                     getlocal2
                     ifstrictne ofs00a4
                     pushbyte 5
                     jump ofs00c0
            ofs00a4:
                     pushstring "G"
                     getlocal2
                     ifstrictne ofs00b1
                     pushbyte 6
                     jump ofs00c0
            ofs00b1:
                     pushstring "H"
                     getlocal2
                     ifstrictne ofs00be
                     pushbyte 7
                     jump ofs00c0
            ofs00be:
                     pushbyte -1
            ofs00c0:
                     kill 2
                     lookupswitch ofs0027, [ofs000f, ofs001b, ofs001b, ofs0027, ofs0027, ofs0033, ofs003b, ofs0047]
            ofs00df:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchBig"),ProtectedNamespace("tests:TestSwitchBig"),StaticProtectedNs("tests:TestSwitchBig"),PrivateNamespace("TestSwitchBig.as$0")])
                     pushstring "after switch"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchBig"),ProtectedNamespace("tests:TestSwitchBig"),StaticProtectedNs("tests:TestSwitchBig"),PrivateNamespace("TestSwitchBig.as$0")]), 1
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
            findpropstrict Multiname("TestSwitchBig",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestSwitchBig")
            returnvoid
         end ; code
      end ; body
   end ; method
   
