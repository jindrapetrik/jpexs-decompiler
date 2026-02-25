package tests
{
   public class TestGotos7
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
      
      public function TestGotos7()
      {
         method
            name "tests:TestGotos7/TestGotos7"
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
      
      public function run() : void
      {
         trait method QName(PackageNamespace(""),"run")
            dispid 0
            method
               name "tests:TestGotos7/run"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 2
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "i", 0, 13
                     pushbyte 0
                     convert_i
                     setlocal1
                     jump ofs00ac
            ofs000f:
                     label
                     jump ofs0058
            ofs0014:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos7"),ProtectedNamespace("tests:TestGotos7"),StaticProtectedNs("tests:TestGotos7"),PrivateNamespace("TestGotos7.as$0")])
                     pushstring "zero"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos7"),ProtectedNamespace("tests:TestGotos7"),StaticProtectedNs("tests:TestGotos7"),PrivateNamespace("TestGotos7.as$0")]), 1
                     jump ofs00aa
            ofs0020:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos7"),ProtectedNamespace("tests:TestGotos7"),StaticProtectedNs("tests:TestGotos7"),PrivateNamespace("TestGotos7.as$0")])
                     pushstring "five"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos7"),ProtectedNamespace("tests:TestGotos7"),StaticProtectedNs("tests:TestGotos7"),PrivateNamespace("TestGotos7.as$0")]), 1
                     jump ofs00a3
            ofs002c:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos7"),ProtectedNamespace("tests:TestGotos7"),StaticProtectedNs("tests:TestGotos7"),PrivateNamespace("TestGotos7.as$0")])
                     pushstring "ten"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos7"),ProtectedNamespace("tests:TestGotos7"),StaticProtectedNs("tests:TestGotos7"),PrivateNamespace("TestGotos7.as$0")]), 1
                     jump ofs00a3
            ofs0038:
                     label
                     getlocal1
                     pushbyte 7
                     ifne ofs0044
                     jump ofs00aa
            ofs0044:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos7"),ProtectedNamespace("tests:TestGotos7"),StaticProtectedNs("tests:TestGotos7"),PrivateNamespace("TestGotos7.as$0")])
                     pushstring "one"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos7"),ProtectedNamespace("tests:TestGotos7"),StaticProtectedNs("tests:TestGotos7"),PrivateNamespace("TestGotos7.as$0")]), 1
            ofs004b:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos7"),ProtectedNamespace("tests:TestGotos7"),StaticProtectedNs("tests:TestGotos7"),PrivateNamespace("TestGotos7.as$0")])
                     pushstring "def"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos7"),ProtectedNamespace("tests:TestGotos7"),StaticProtectedNs("tests:TestGotos7"),PrivateNamespace("TestGotos7.as$0")]), 1
                     label
                     jump ofs00a3
            ofs0058:
                     getlocal1
                     setlocal2
                     pushbyte 0
                     getlocal2
                     ifstrictne ofs0067
                     pushbyte 0
                     jump ofs0090
            ofs0067:
                     pushbyte 5
                     getlocal2
                     ifstrictne ofs0074
                     pushbyte 1
                     jump ofs0090
            ofs0074:
                     pushbyte 10
                     getlocal2
                     ifstrictne ofs0081
                     pushbyte 2
                     jump ofs0090
            ofs0081:
                     pushbyte 1
                     getlocal2
                     ifstrictne ofs008e
                     pushbyte 3
                     jump ofs0090
            ofs008e:
                     pushbyte -1
            ofs0090:
                     kill 2
                     lookupswitch ofs004b, [ofs0014, ofs0020, ofs002c, ofs0038]
            ofs00a3:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos7"),ProtectedNamespace("tests:TestGotos7"),StaticProtectedNs("tests:TestGotos7"),PrivateNamespace("TestGotos7.as$0")])
                     pushstring "before loop end"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos7"),ProtectedNamespace("tests:TestGotos7"),StaticProtectedNs("tests:TestGotos7"),PrivateNamespace("TestGotos7.as$0")]), 1
            ofs00aa:
                     inclocal_i 1
            ofs00ac:
                     getlocal1
                     pushbyte 10
                     iflt ofs000f
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
            findpropstrict Multiname("TestGotos7",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestGotos7")
            returnvoid
         end ; code
      end ; body
   end ; method
   
