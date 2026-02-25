package tests
{
   public class TestForContinue
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
      
      public function TestForContinue()
      {
         method
            name "tests:TestForContinue/TestForContinue"
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
               name "tests:TestForContinue/run"
               returns null
               
               body
                  maxstack 3
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     pushbyte 0
                     coerce_a
                     setlocal1
                     jump ofs0063
            ofs000f:
                     label
                     getlocal1
                     pushbyte 9
                     ifne ofs004f
                     getlocal1
                     pushbyte 5
                     ifne ofs0029
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForContinue"),ProtectedNamespace("tests:TestForContinue"),StaticProtectedNs("tests:TestForContinue"),PrivateNamespace("TestForContinue.as$0")])
                     pushstring "part1"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForContinue"),ProtectedNamespace("tests:TestForContinue"),StaticProtectedNs("tests:TestForContinue"),PrivateNamespace("TestForContinue.as$0")]), 1
                     jump ofs005d
            ofs0029:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForContinue"),ProtectedNamespace("tests:TestForContinue"),StaticProtectedNs("tests:TestForContinue"),PrivateNamespace("TestForContinue.as$0")])
                     pushstring "a="
                     getlocal1
                     add
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForContinue"),ProtectedNamespace("tests:TestForContinue"),StaticProtectedNs("tests:TestForContinue"),PrivateNamespace("TestForContinue.as$0")]), 1
                     getlocal1
                     pushbyte 7
                     ifne ofs0044
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForContinue"),ProtectedNamespace("tests:TestForContinue"),StaticProtectedNs("tests:TestForContinue"),PrivateNamespace("TestForContinue.as$0")])
                     pushstring "part2"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForContinue"),ProtectedNamespace("tests:TestForContinue"),StaticProtectedNs("tests:TestForContinue"),PrivateNamespace("TestForContinue.as$0")]), 1
                     jump ofs005d
            ofs0044:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForContinue"),ProtectedNamespace("tests:TestForContinue"),StaticProtectedNs("tests:TestForContinue"),PrivateNamespace("TestForContinue.as$0")])
                     pushstring "part3"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForContinue"),ProtectedNamespace("tests:TestForContinue"),StaticProtectedNs("tests:TestForContinue"),PrivateNamespace("TestForContinue.as$0")]), 1
                     jump ofs0056
            ofs004f:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForContinue"),ProtectedNamespace("tests:TestForContinue"),StaticProtectedNs("tests:TestForContinue"),PrivateNamespace("TestForContinue.as$0")])
                     pushstring "part4"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForContinue"),ProtectedNamespace("tests:TestForContinue"),StaticProtectedNs("tests:TestForContinue"),PrivateNamespace("TestForContinue.as$0")]), 1
            ofs0056:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForContinue"),ProtectedNamespace("tests:TestForContinue"),StaticProtectedNs("tests:TestForContinue"),PrivateNamespace("TestForContinue.as$0")])
                     pushstring "part5"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForContinue"),ProtectedNamespace("tests:TestForContinue"),StaticProtectedNs("tests:TestForContinue"),PrivateNamespace("TestForContinue.as$0")]), 1
            ofs005d:
                     getlocal1
                     pushbyte 1
                     add
                     coerce_a
                     setlocal1
            ofs0063:
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
            findpropstrict Multiname("TestForContinue",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestForContinue")
            returnvoid
         end ; code
      end ; body
   end ; method
   
