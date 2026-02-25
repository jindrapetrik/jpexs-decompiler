package tests
{
   public class TestDoWhileTwice
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
      
      public function TestDoWhileTwice()
      {
         method
            name "tests:TestDoWhileTwice/TestDoWhileTwice"
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
               name "tests:TestDoWhileTwice/run"
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
                     pushbyte 1
                     convert_i
                     setlocal1
                     pushbyte 2
                     convert_i
                     setlocal2
                     jump ofs0019
            ofs0018:
                     label
            ofs0019:
                     jump ofs001e
            ofs001d:
                     label
            ofs001e:
                     getlocal1
                     convert_b
                     iffalse ofs003c
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhileTwice"),ProtectedNamespace("tests:TestDoWhileTwice"),StaticProtectedNs("tests:TestDoWhileTwice"),PrivateNamespace("TestDoWhileTwice.as$0")])
                     pushstring "x"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhileTwice"),ProtectedNamespace("tests:TestDoWhileTwice"),StaticProtectedNs("tests:TestDoWhileTwice"),PrivateNamespace("TestDoWhileTwice.as$0")]), 1
                     getlocal2
                     convert_b
                     iffalse ofs0035
                     jump ofs0048
            ofs0035:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhileTwice"),ProtectedNamespace("tests:TestDoWhileTwice"),StaticProtectedNs("tests:TestDoWhileTwice"),PrivateNamespace("TestDoWhileTwice.as$0")])
                     pushstring "y"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhileTwice"),ProtectedNamespace("tests:TestDoWhileTwice"),StaticProtectedNs("tests:TestDoWhileTwice"),PrivateNamespace("TestDoWhileTwice.as$0")]), 1
            ofs003c:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhileTwice"),ProtectedNamespace("tests:TestDoWhileTwice"),StaticProtectedNs("tests:TestDoWhileTwice"),PrivateNamespace("TestDoWhileTwice.as$0")])
                     pushstring "z"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhileTwice"),ProtectedNamespace("tests:TestDoWhileTwice"),StaticProtectedNs("tests:TestDoWhileTwice"),PrivateNamespace("TestDoWhileTwice.as$0")]), 1
                     pushtrue
                     iftrue ofs001d
            ofs0048:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhileTwice"),ProtectedNamespace("tests:TestDoWhileTwice"),StaticProtectedNs("tests:TestDoWhileTwice"),PrivateNamespace("TestDoWhileTwice.as$0")])
                     pushstring "g"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhileTwice"),ProtectedNamespace("tests:TestDoWhileTwice"),StaticProtectedNs("tests:TestDoWhileTwice"),PrivateNamespace("TestDoWhileTwice.as$0")]), 1
                     getlocal2
                     convert_b
                     iffalse ofs0059
                     jump ofs0065
            ofs0059:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhileTwice"),ProtectedNamespace("tests:TestDoWhileTwice"),StaticProtectedNs("tests:TestDoWhileTwice"),PrivateNamespace("TestDoWhileTwice.as$0")])
                     pushstring "h"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhileTwice"),ProtectedNamespace("tests:TestDoWhileTwice"),StaticProtectedNs("tests:TestDoWhileTwice"),PrivateNamespace("TestDoWhileTwice.as$0")]), 1
                     pushtrue
                     iftrue ofs0018
            ofs0065:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhileTwice"),ProtectedNamespace("tests:TestDoWhileTwice"),StaticProtectedNs("tests:TestDoWhileTwice"),PrivateNamespace("TestDoWhileTwice.as$0")])
                     pushstring "finish"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhileTwice"),ProtectedNamespace("tests:TestDoWhileTwice"),StaticProtectedNs("tests:TestDoWhileTwice"),PrivateNamespace("TestDoWhileTwice.as$0")]), 1
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
            findpropstrict Multiname("TestDoWhileTwice",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestDoWhileTwice")
            returnvoid
         end ; code
      end ; body
   end ; method
   
