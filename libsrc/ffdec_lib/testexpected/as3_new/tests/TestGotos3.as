package tests
{
   public class TestGotos3
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
      
      public function TestGotos3()
      {
         method
            name "tests:TestGotos3/TestGotos3"
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
               name "tests:TestGotos3/run"
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
                     debug 1, "a", 1, 14
                     pushbyte 0
                     convert_i
                     setlocal1
                     pushbyte 5
                     convert_i
                     setlocal2
                     getlocal2
                     pushbyte 5
                     ifngt ofs0051
                     pushbyte 0
                     convert_i
                     setlocal1
                     jump ofs0046
            ofs0023:
                     label
                     getlocal1
                     pushbyte 3
                     ifngt ofs003d
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos3"),ProtectedNamespace("tests:TestGotos3"),StaticProtectedNs("tests:TestGotos3"),PrivateNamespace("TestGotos3.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos3"),ProtectedNamespace("tests:TestGotos3"),StaticProtectedNs("tests:TestGotos3"),PrivateNamespace("TestGotos3.as$0")]), 1
                     getlocal1
                     pushbyte 4
                     ifne ofs003d
                     jump ofs004d
            ofs003d:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos3"),ProtectedNamespace("tests:TestGotos3"),StaticProtectedNs("tests:TestGotos3"),PrivateNamespace("TestGotos3.as$0")])
                     pushstring "B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos3"),ProtectedNamespace("tests:TestGotos3"),StaticProtectedNs("tests:TestGotos3"),PrivateNamespace("TestGotos3.as$0")]), 1
                     inclocal_i 1
            ofs0046:
                     getlocal1
                     pushbyte 5
                     iflt ofs0023
            ofs004d:
                     jump ofs0058
            ofs0051:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos3"),ProtectedNamespace("tests:TestGotos3"),StaticProtectedNs("tests:TestGotos3"),PrivateNamespace("TestGotos3.as$0")])
                     pushstring "C"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos3"),ProtectedNamespace("tests:TestGotos3"),StaticProtectedNs("tests:TestGotos3"),PrivateNamespace("TestGotos3.as$0")]), 1
            ofs0058:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos3"),ProtectedNamespace("tests:TestGotos3"),StaticProtectedNs("tests:TestGotos3"),PrivateNamespace("TestGotos3.as$0")])
                     pushstring "return"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos3"),ProtectedNamespace("tests:TestGotos3"),StaticProtectedNs("tests:TestGotos3"),PrivateNamespace("TestGotos3.as$0")]), 1
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
            findpropstrict Multiname("TestGotos3",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestGotos3")
            returnvoid
         end ; code
      end ; body
   end ; method
   
