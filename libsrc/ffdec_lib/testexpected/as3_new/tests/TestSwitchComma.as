package tests
{
   public class TestSwitchComma
   {
      
      private static const X:int = 7;
      
      method
         name ""
         returns null
         
         body
            maxstack 2
            localcount 1
            initscopedepth 3
            maxscopedepth 4
            
            code
               getlocal0
               pushscope
               findproperty QName(PrivateNamespace("tests:TestSwitchComma"),"X")
               pushbyte 7
               initproperty QName(PrivateNamespace("tests:TestSwitchComma"),"X")
               returnvoid
            end ; code
         end ; body
      end ; method
      
      public function TestSwitchComma()
      {
         method
            name "tests:TestSwitchComma/TestSwitchComma"
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
               name "tests:TestSwitchComma/run"
               returns null
               
               body
                  maxstack 2
                  localcount 4
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "b", 0, 15
                     debug 1, "a", 1, 16
                     pushbyte 5
                     convert_i
                     setlocal1
                     pushstring "A"
                     coerce_s
                     setlocal2
                     jump ofs0039
            ofs0018:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchComma"),ProtectedNamespace("tests:TestSwitchComma"),StaticProtectedNs("tests:TestSwitchComma"),PrivateNamespace("TestSwitchComma.as$0")])
                     pushstring "is A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchComma"),ProtectedNamespace("tests:TestSwitchComma"),StaticProtectedNs("tests:TestSwitchComma"),PrivateNamespace("TestSwitchComma.as$0")]), 1
                     jump ofs0079
            ofs0024:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchComma"),ProtectedNamespace("tests:TestSwitchComma"),StaticProtectedNs("tests:TestSwitchComma"),PrivateNamespace("TestSwitchComma.as$0")])
                     pushstring "is B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchComma"),ProtectedNamespace("tests:TestSwitchComma"),StaticProtectedNs("tests:TestSwitchComma"),PrivateNamespace("TestSwitchComma.as$0")]), 1
            ofs002c:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchComma"),ProtectedNamespace("tests:TestSwitchComma"),StaticProtectedNs("tests:TestSwitchComma"),PrivateNamespace("TestSwitchComma.as$0")])
                     pushstring "is C"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSwitchComma"),ProtectedNamespace("tests:TestSwitchComma"),StaticProtectedNs("tests:TestSwitchComma"),PrivateNamespace("TestSwitchComma.as$0")]), 1
            ofs0034:
                     label
                     jump ofs0079
            ofs0039:
                     getlocal2
                     setlocal3
                     pushstring "A"
                     getlocal3
                     ifstrictne ofs0048
                     pushbyte 0
                     jump ofs0069
            ofs0048:
                     pushstring "B"
                     getlocal3
                     ifstrictne ofs0055
                     pushbyte 1
                     jump ofs0069
            ofs0055:
                     getlex QName(PackageNamespace("tests"),"TestSwitchComma")
                     getproperty QName(PrivateNamespace("tests:TestSwitchComma"),"X")
                     pop
                     pushstring "C"
                     getlocal3
                     ifstrictne ofs0067
                     pushbyte 2
                     jump ofs0069
            ofs0067:
                     pushbyte -1
            ofs0069:
                     kill 3
                     lookupswitch ofs0034, [ofs0018, ofs0024, ofs002c]
            ofs0079:
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
            findpropstrict Multiname("TestSwitchComma",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestSwitchComma")
            returnvoid
         end ; code
      end ; body
   end ; method
   
