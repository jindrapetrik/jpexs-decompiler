package tests
{
   public class TestGotos6
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
      
      public function TestGotos6()
      {
         method
            name "tests:TestGotos6/TestGotos6"
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
               name "tests:TestGotos6/run"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 2
                  localcount 4
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     debug 1, "s", 1, 14
                     pushtrue
                     convert_b
                     setlocal1
                     pushstring "a"
                     coerce_s
                     setlocal2
                     getlocal1
                     iffalse ofs007c
                     jump ofs003d
            ofs001c:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos6"),ProtectedNamespace("tests:TestGotos6"),StaticProtectedNs("tests:TestGotos6"),PrivateNamespace("TestGotos6.as$0")])
                     pushstring "is A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos6"),ProtectedNamespace("tests:TestGotos6"),StaticProtectedNs("tests:TestGotos6"),PrivateNamespace("TestGotos6.as$0")]), 1
                     jump ofs0078
            ofs0028:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos6"),ProtectedNamespace("tests:TestGotos6"),StaticProtectedNs("tests:TestGotos6"),PrivateNamespace("TestGotos6.as$0")])
                     pushstring "is B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos6"),ProtectedNamespace("tests:TestGotos6"),StaticProtectedNs("tests:TestGotos6"),PrivateNamespace("TestGotos6.as$0")]), 1
            ofs0030:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos6"),ProtectedNamespace("tests:TestGotos6"),StaticProtectedNs("tests:TestGotos6"),PrivateNamespace("TestGotos6.as$0")])
                     pushstring "is BC"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos6"),ProtectedNamespace("tests:TestGotos6"),StaticProtectedNs("tests:TestGotos6"),PrivateNamespace("TestGotos6.as$0")]), 1
            ofs0038:
                     label
                     jump ofs0078
            ofs003d:
                     getlocal2
                     setlocal3
                     pushstring "a"
                     getlocal3
                     ifstrictne ofs004c
                     pushbyte 0
                     jump ofs0068
            ofs004c:
                     pushstring "b"
                     getlocal3
                     ifstrictne ofs0059
                     pushbyte 1
                     jump ofs0068
            ofs0059:
                     pushstring "c"
                     getlocal3
                     ifstrictne ofs0066
                     pushbyte 2
                     jump ofs0068
            ofs0066:
                     pushbyte -1
            ofs0068:
                     kill 3
                     lookupswitch ofs0038, [ofs001c, ofs0028, ofs0030]
            ofs0078:
                     jump ofs0083
            ofs007c:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos6"),ProtectedNamespace("tests:TestGotos6"),StaticProtectedNs("tests:TestGotos6"),PrivateNamespace("TestGotos6.as$0")])
                     pushstring "D"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos6"),ProtectedNamespace("tests:TestGotos6"),StaticProtectedNs("tests:TestGotos6"),PrivateNamespace("TestGotos6.as$0")]), 1
            ofs0083:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos6"),ProtectedNamespace("tests:TestGotos6"),StaticProtectedNs("tests:TestGotos6"),PrivateNamespace("TestGotos6.as$0")])
                     pushstring "finish"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos6"),ProtectedNamespace("tests:TestGotos6"),StaticProtectedNs("tests:TestGotos6"),PrivateNamespace("TestGotos6.as$0")]), 1
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
            findpropstrict Multiname("TestGotos6",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestGotos6")
            returnvoid
         end ; code
      end ; body
   end ; method
   
