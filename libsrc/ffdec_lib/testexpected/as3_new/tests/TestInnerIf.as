package tests
{
   public class TestInnerIf
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
      
      public function TestInnerIf()
      {
         method
            name "tests:TestInnerIf/TestInnerIf"
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
               name "tests:TestInnerIf/run"
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
                     debug 1, "b", 1, 14
                     pushbyte 5
                     coerce_a
                     setlocal1
                     pushbyte 4
                     coerce_a
                     setlocal2
                     getlocal1
                     pushbyte 5
                     ifne ofs0038
                     getlocal2
                     pushbyte 6
                     ifne ofs002d
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerIf"),ProtectedNamespace("tests:TestInnerIf"),StaticProtectedNs("tests:TestInnerIf"),PrivateNamespace("TestInnerIf.as$0")])
                     pushstring "b==6"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerIf"),ProtectedNamespace("tests:TestInnerIf"),StaticProtectedNs("tests:TestInnerIf"),PrivateNamespace("TestInnerIf.as$0")]), 1
                     jump ofs0034
            ofs002d:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerIf"),ProtectedNamespace("tests:TestInnerIf"),StaticProtectedNs("tests:TestInnerIf"),PrivateNamespace("TestInnerIf.as$0")])
                     pushstring "b!=6"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerIf"),ProtectedNamespace("tests:TestInnerIf"),StaticProtectedNs("tests:TestInnerIf"),PrivateNamespace("TestInnerIf.as$0")]), 1
            ofs0034:
                     jump ofs0051
            ofs0038:
                     getlocal2
                     pushbyte 7
                     ifne ofs004a
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerIf"),ProtectedNamespace("tests:TestInnerIf"),StaticProtectedNs("tests:TestInnerIf"),PrivateNamespace("TestInnerIf.as$0")])
                     pushstring "b==7"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerIf"),ProtectedNamespace("tests:TestInnerIf"),StaticProtectedNs("tests:TestInnerIf"),PrivateNamespace("TestInnerIf.as$0")]), 1
                     jump ofs0051
            ofs004a:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerIf"),ProtectedNamespace("tests:TestInnerIf"),StaticProtectedNs("tests:TestInnerIf"),PrivateNamespace("TestInnerIf.as$0")])
                     pushstring "b!=7"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerIf"),ProtectedNamespace("tests:TestInnerIf"),StaticProtectedNs("tests:TestInnerIf"),PrivateNamespace("TestInnerIf.as$0")]), 1
            ofs0051:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerIf"),ProtectedNamespace("tests:TestInnerIf"),StaticProtectedNs("tests:TestInnerIf"),PrivateNamespace("TestInnerIf.as$0")])
                     pushstring "end"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestInnerIf"),ProtectedNamespace("tests:TestInnerIf"),StaticProtectedNs("tests:TestInnerIf"),PrivateNamespace("TestInnerIf.as$0")]), 1
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
            findpropstrict Multiname("TestInnerIf",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestInnerIf")
            returnvoid
         end ; code
      end ; body
   end ; method
   
