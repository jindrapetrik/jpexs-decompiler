package tests
{
   public class TestContinueLevels
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
      
      public function TestContinueLevels()
      {
         method
            name "tests:TestContinueLevels/TestContinueLevels"
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
               name "tests:TestContinueLevels/run"
               returns null
               
               body
                  maxstack 2
                  localcount 7
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "b", 0, 13
                     debug 1, "c", 1, 14
                     debug 1, "d", 2, 15
                     debug 1, "e", 3, 16
                     debug 1, "a", 4, 17
                     pushundefined
                     coerce_a
                     setlocal1
                     pushundefined
                     coerce_a
                     setlocal2
                     pushundefined
                     coerce_a
                     setlocal3
                     pushundefined
                     coerce_a
                     setlocal 4
                     pushbyte 5
                     coerce_a
                     setlocal 5
                     jump ofs0096
            ofs0031:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestContinueLevels"),ProtectedNamespace("tests:TestContinueLevels"),StaticProtectedNs("tests:TestContinueLevels"),PrivateNamespace("TestContinueLevels.as$0")])
                     pushstring "fiftyseven multiply a"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestContinueLevels"),ProtectedNamespace("tests:TestContinueLevels"),StaticProtectedNs("tests:TestContinueLevels"),PrivateNamespace("TestContinueLevels.as$0")]), 1
                     pushbyte 0
                     coerce_a
                     setlocal1
                     jump ofs005e
            ofs0041:
                     label
                     getlocal1
                     pushbyte 10
                     ifne ofs004d
                     jump ofs0065
            ofs004d:
                     getlocal1
                     pushbyte 15
                     ifne ofs0058
                     jump ofs0065
            ofs0058:
                     getlocal1
                     pushbyte 1
                     add
                     coerce_a
                     setlocal1
            ofs005e:
                     getlocal1
                     pushbyte 50
                     iflt ofs0041
            ofs0065:
                     jump ofs00ea
            ofs0069:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestContinueLevels"),ProtectedNamespace("tests:TestContinueLevels"),StaticProtectedNs("tests:TestContinueLevels"),PrivateNamespace("TestContinueLevels.as$0")])
                     pushstring "thirteen"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestContinueLevels"),ProtectedNamespace("tests:TestContinueLevels"),StaticProtectedNs("tests:TestContinueLevels"),PrivateNamespace("TestContinueLevels.as$0")]), 1
            ofs0071:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestContinueLevels"),ProtectedNamespace("tests:TestContinueLevels"),StaticProtectedNs("tests:TestContinueLevels"),PrivateNamespace("TestContinueLevels.as$0")])
                     pushstring "fourteen"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestContinueLevels"),ProtectedNamespace("tests:TestContinueLevels"),StaticProtectedNs("tests:TestContinueLevels"),PrivateNamespace("TestContinueLevels.as$0")]), 1
                     jump ofs00ea
            ofs007d:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestContinueLevels"),ProtectedNamespace("tests:TestContinueLevels"),StaticProtectedNs("tests:TestContinueLevels"),PrivateNamespace("TestContinueLevels.as$0")])
                     pushstring "eightynine"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestContinueLevels"),ProtectedNamespace("tests:TestContinueLevels"),StaticProtectedNs("tests:TestContinueLevels"),PrivateNamespace("TestContinueLevels.as$0")]), 1
                     jump ofs00ea
            ofs0089:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestContinueLevels"),ProtectedNamespace("tests:TestContinueLevels"),StaticProtectedNs("tests:TestContinueLevels"),PrivateNamespace("TestContinueLevels.as$0")])
                     pushstring "default clause"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestContinueLevels"),ProtectedNamespace("tests:TestContinueLevels"),StaticProtectedNs("tests:TestContinueLevels"),PrivateNamespace("TestContinueLevels.as$0")]), 1
                     label
                     jump ofs00ea
            ofs0096:
                     getlocal 5
                     setlocal 6
                     pushbyte 57
                     getlocal 5
                     multiply
                     getlocal 6
                     ifstrictne ofs00ab
                     pushbyte 0
                     jump ofs00d7
            ofs00ab:
                     pushbyte 13
                     getlocal 6
                     ifstrictne ofs00b9
                     pushbyte 1
                     jump ofs00d7
            ofs00b9:
                     pushbyte 14
                     getlocal 6
                     ifstrictne ofs00c7
                     pushbyte 2
                     jump ofs00d7
            ofs00c7:
                     pushbyte 89
                     getlocal 6
                     ifstrictne ofs00d5
                     pushbyte 3
                     jump ofs00d7
            ofs00d5:
                     pushbyte -1
            ofs00d7:
                     kill 6
                     lookupswitch ofs0089, [ofs0031, ofs0069, ofs0071, ofs007d]
            ofs00ea:
                     pushbyte 0
                     coerce_a
                     setlocal2
                     jump ofs0143
            ofs00f2:
                     label
                     pushbyte 0
                     coerce_a
                     setlocal3
                     jump ofs012f
            ofs00fb:
                     label
                     pushbyte 0
                     coerce_a
                     setlocal 4
                     getlocal 4
                     pushbyte 50
                     ifnlt ofs012d
                     getlocal 4
                     pushbyte 9
                     ifne ofs0115
                     jump ofs0136
            ofs0115:
                     getlocal 4
                     pushbyte 20
                     ifne ofs0121
                     jump ofs013d
            ofs0121:
                     getlocal 4
                     pushbyte 8
                     ifeq ofs012d
                     jump ofs014a
            ofs012d:
                     inclocal 3
            ofs012f:
                     getlocal3
                     pushbyte 25
                     iflt ofs00fb
            ofs0136:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestContinueLevels"),ProtectedNamespace("tests:TestContinueLevels"),StaticProtectedNs("tests:TestContinueLevels"),PrivateNamespace("TestContinueLevels.as$0")])
                     pushstring "hello"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestContinueLevels"),ProtectedNamespace("tests:TestContinueLevels"),StaticProtectedNs("tests:TestContinueLevels"),PrivateNamespace("TestContinueLevels.as$0")]), 1
            ofs013d:
                     getlocal2
                     pushbyte 1
                     add
                     coerce_a
                     setlocal2
            ofs0143:
                     getlocal2
                     pushbyte 8
                     iflt ofs00f2
            ofs014a:
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
            findpropstrict Multiname("TestContinueLevels",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestContinueLevels")
            returnvoid
         end ; code
      end ; body
   end ; method
   
