package tests
{
   public class TestWhileBreak2
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
      
      public function TestWhileBreak2()
      {
         method
            name "tests:TestWhileBreak2/TestWhileBreak2"
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
               name "tests:TestWhileBreak2/run"
               returns null
               
               body
                  maxstack 2
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "k", 0, 13
                     pushbyte 8
                     convert_i
                     setlocal1
                     jump ofs00b8
            ofs000f:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")])
                     pushstring "X"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")]), 1
                     getlocal1
                     pushbyte 1
                     ifne ofs0026
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")]), 1
                     returnvoid
            ofs0026:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")])
                     pushstring "Y"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")]), 1
                     getlocal1
                     pushbyte 10
                     ifnlt ofs009b
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")])
                     pushstring "k1"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")]), 1
                     getlocal1
                     pushbyte 2
                     ifne ofs0062
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")])
                     pushstring "B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")]), 1
                     getlocal1
                     pushbyte 1
                     ifngt ofs005b
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")])
                     pushstring "B1"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")]), 1
                     jump ofs00bd
            ofs005b:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")])
                     pushstring "B2"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")]), 1
            ofs0062:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")])
                     pushstring "Z"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")]), 1
                     getlocal1
                     pushbyte 3
                     ifne ofs007b
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")])
                     pushstring "C"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")]), 1
                     jump ofs00bd
            ofs007b:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")])
                     pushstring "Z2"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")]), 1
                     getlocal1
                     pushbyte 4
                     ifne ofs0094
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")])
                     pushstring "D"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")]), 1
                     jump ofs00bd
            ofs0094:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")])
                     pushstring "k2"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")]), 1
            ofs009b:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")])
                     pushstring "E"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")]), 1
                     getlocal1
                     pushbyte 2
                     ifne ofs00b1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")])
                     pushstring "E1"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")]), 1
                     returnvoid
            ofs00b1:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")])
                     pushstring "gg"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")]), 1
            ofs00b8:
                     pushtrue
                     iftrue ofs000f
            ofs00bd:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")])
                     pushstring "ss"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileBreak2"),ProtectedNamespace("tests:TestWhileBreak2"),StaticProtectedNs("tests:TestWhileBreak2"),PrivateNamespace("TestWhileBreak2.as$0")]), 1
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
            findpropstrict Multiname("TestWhileBreak2",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestWhileBreak2")
            returnvoid
         end ; code
      end ; body
   end ; method
   
