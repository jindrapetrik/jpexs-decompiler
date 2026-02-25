package tests
{
   public class TestForGoto
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
      
      public function TestForGoto()
      {
         method
            name "tests:TestForGoto/TestForGoto"
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
               name "tests:TestForGoto/run"
               returns null
               
               body
                  maxstack 2
                  localcount 4
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "c", 0, 13
                     debug 1, "len", 1, 14
                     debug 1, "i", 2, 15
                     pushbyte 0
                     convert_i
                     setlocal1
                     pushbyte 5
                     convert_i
                     setlocal2
                     pushbyte 0
                     convert_u
                     setlocal3
                     jump ofs0053
            ofs0021:
                     label
                     pushbyte 1
                     convert_i
                     setlocal1
                     getlocal1
                     pushbyte 2
                     ifne ofs0038
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForGoto"),ProtectedNamespace("tests:TestForGoto"),StaticProtectedNs("tests:TestForGoto"),PrivateNamespace("TestForGoto.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForGoto"),ProtectedNamespace("tests:TestForGoto"),StaticProtectedNs("tests:TestForGoto"),PrivateNamespace("TestForGoto.as$0")]), 1
                     jump ofs004a
            ofs0038:
                     getlocal1
                     pushbyte 3
                     ifeq ofs0043
                     jump ofs0051
            ofs0043:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForGoto"),ProtectedNamespace("tests:TestForGoto"),StaticProtectedNs("tests:TestForGoto"),PrivateNamespace("TestForGoto.as$0")])
                     pushstring "B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForGoto"),ProtectedNamespace("tests:TestForGoto"),StaticProtectedNs("tests:TestForGoto"),PrivateNamespace("TestForGoto.as$0")]), 1
            ofs004a:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForGoto"),ProtectedNamespace("tests:TestForGoto"),StaticProtectedNs("tests:TestForGoto"),PrivateNamespace("TestForGoto.as$0")])
                     pushstring "C"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForGoto"),ProtectedNamespace("tests:TestForGoto"),StaticProtectedNs("tests:TestForGoto"),PrivateNamespace("TestForGoto.as$0")]), 1
            ofs0051:
                     inclocal 3
            ofs0053:
                     getlocal3
                     getlocal2
                     iflt ofs0021
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForGoto"),ProtectedNamespace("tests:TestForGoto"),StaticProtectedNs("tests:TestForGoto"),PrivateNamespace("TestForGoto.as$0")])
                     pushstring "exit"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForGoto"),ProtectedNamespace("tests:TestForGoto"),StaticProtectedNs("tests:TestForGoto"),PrivateNamespace("TestForGoto.as$0")]), 1
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
            findpropstrict Multiname("TestForGoto",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestForGoto")
            returnvoid
         end ; code
      end ; body
   end ; method
   
