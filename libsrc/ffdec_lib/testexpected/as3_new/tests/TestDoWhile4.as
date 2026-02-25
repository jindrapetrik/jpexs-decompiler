package tests
{
   public class TestDoWhile4
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
      
      public function TestDoWhile4()
      {
         method
            name "tests:TestDoWhile4/TestDoWhile4"
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
               name "tests:TestDoWhile4/run"
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
                     jump ofs0010
            ofs000f:
                     label
            ofs0010:
                     getlocal1
                     pushbyte 9
                     ifne ofs0037
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhile4"),ProtectedNamespace("tests:TestDoWhile4"),StaticProtectedNs("tests:TestDoWhile4"),PrivateNamespace("TestDoWhile4.as$0")])
                     pushstring "h"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhile4"),ProtectedNamespace("tests:TestDoWhile4"),StaticProtectedNs("tests:TestDoWhile4"),PrivateNamespace("TestDoWhile4.as$0")]), 1
                     getlocal1
                     pushbyte 9
                     ifne ofs0030
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhile4"),ProtectedNamespace("tests:TestDoWhile4"),StaticProtectedNs("tests:TestDoWhile4"),PrivateNamespace("TestDoWhile4.as$0")])
                     pushstring "f"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhile4"),ProtectedNamespace("tests:TestDoWhile4"),StaticProtectedNs("tests:TestDoWhile4"),PrivateNamespace("TestDoWhile4.as$0")]), 1
                     jump ofs003e
            ofs0030:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhile4"),ProtectedNamespace("tests:TestDoWhile4"),StaticProtectedNs("tests:TestDoWhile4"),PrivateNamespace("TestDoWhile4.as$0")])
                     pushstring "b"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhile4"),ProtectedNamespace("tests:TestDoWhile4"),StaticProtectedNs("tests:TestDoWhile4"),PrivateNamespace("TestDoWhile4.as$0")]), 1
            ofs0037:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhile4"),ProtectedNamespace("tests:TestDoWhile4"),StaticProtectedNs("tests:TestDoWhile4"),PrivateNamespace("TestDoWhile4.as$0")])
                     pushstring "gg"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhile4"),ProtectedNamespace("tests:TestDoWhile4"),StaticProtectedNs("tests:TestDoWhile4"),PrivateNamespace("TestDoWhile4.as$0")]), 1
            ofs003e:
                     getlocal1
                     pushbyte 10
                     iflt ofs000f
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhile4"),ProtectedNamespace("tests:TestDoWhile4"),StaticProtectedNs("tests:TestDoWhile4"),PrivateNamespace("TestDoWhile4.as$0")])
                     pushstring "ss"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhile4"),ProtectedNamespace("tests:TestDoWhile4"),StaticProtectedNs("tests:TestDoWhile4"),PrivateNamespace("TestDoWhile4.as$0")]), 1
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
            findpropstrict Multiname("TestDoWhile4",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestDoWhile4")
            returnvoid
         end ; code
      end ; body
   end ; method
   
