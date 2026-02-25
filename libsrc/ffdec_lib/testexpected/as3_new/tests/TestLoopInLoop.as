package tests
{
   public class TestLoopInLoop
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
      
      public function TestLoopInLoop()
      {
         method
            name "tests:TestLoopInLoop/TestLoopInLoop"
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
               name "tests:TestLoopInLoop/run"
               returns null
               
               body
                  maxstack 2
                  localcount 5
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "i", 0, 13
                     debug 1, "a", 1, 14
                     debug 1, "b", 2, 15
                     debug 1, "c", 3, 16
                     pushbyte 0
                     convert_i
                     setlocal1
                     pushtrue
                     convert_b
                     setlocal2
                     pushtrue
                     convert_b
                     setlocal3
                     pushtrue
                     convert_b
                     setlocal 4
                     jump ofs008d
            ofs0028:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestLoopInLoop"),ProtectedNamespace("tests:TestLoopInLoop"),StaticProtectedNs("tests:TestLoopInLoop"),PrivateNamespace("TestLoopInLoop.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestLoopInLoop"),ProtectedNamespace("tests:TestLoopInLoop"),StaticProtectedNs("tests:TestLoopInLoop"),PrivateNamespace("TestLoopInLoop.as$0")]), 1
                     pushbyte 0
                     convert_i
                     setlocal1
                     jump ofs007d
            ofs0038:
                     label
                     getlocal2
                     not
                     iffalse ofs007b
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestLoopInLoop"),ProtectedNamespace("tests:TestLoopInLoop"),StaticProtectedNs("tests:TestLoopInLoop"),PrivateNamespace("TestLoopInLoop.as$0")])
                     pushstring "B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestLoopInLoop"),ProtectedNamespace("tests:TestLoopInLoop"),StaticProtectedNs("tests:TestLoopInLoop"),PrivateNamespace("TestLoopInLoop.as$0")]), 1
                     getlocal 4
                     iffalse ofs0057
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestLoopInLoop"),ProtectedNamespace("tests:TestLoopInLoop"),StaticProtectedNs("tests:TestLoopInLoop"),PrivateNamespace("TestLoopInLoop.as$0")])
                     pushstring "C"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestLoopInLoop"),ProtectedNamespace("tests:TestLoopInLoop"),StaticProtectedNs("tests:TestLoopInLoop"),PrivateNamespace("TestLoopInLoop.as$0")]), 1
                     jump ofs006e
            ofs0057:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestLoopInLoop"),ProtectedNamespace("tests:TestLoopInLoop"),StaticProtectedNs("tests:TestLoopInLoop"),PrivateNamespace("TestLoopInLoop.as$0")])
                     pushstring "D"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestLoopInLoop"),ProtectedNamespace("tests:TestLoopInLoop"),StaticProtectedNs("tests:TestLoopInLoop"),PrivateNamespace("TestLoopInLoop.as$0")]), 1
                     getlocal3
                     iffalse ofs0067
                     jump ofs007b
            ofs0067:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestLoopInLoop"),ProtectedNamespace("tests:TestLoopInLoop"),StaticProtectedNs("tests:TestLoopInLoop"),PrivateNamespace("TestLoopInLoop.as$0")])
                     pushstring "H"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestLoopInLoop"),ProtectedNamespace("tests:TestLoopInLoop"),StaticProtectedNs("tests:TestLoopInLoop"),PrivateNamespace("TestLoopInLoop.as$0")]), 1
            ofs006e:
                     getlocal 4
                     iffalse ofs007b
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestLoopInLoop"),ProtectedNamespace("tests:TestLoopInLoop"),StaticProtectedNs("tests:TestLoopInLoop"),PrivateNamespace("TestLoopInLoop.as$0")])
                     pushstring "L"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestLoopInLoop"),ProtectedNamespace("tests:TestLoopInLoop"),StaticProtectedNs("tests:TestLoopInLoop"),PrivateNamespace("TestLoopInLoop.as$0")]), 1
            ofs007b:
                     inclocal_i 1
            ofs007d:
                     getlocal1
                     pushbyte 10
                     iflt ofs0038
                     getlocal2
                     iffalse ofs008d
                     jump ofs0092
            ofs008d:
                     pushtrue
                     iftrue ofs0028
            ofs0092:
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
            findpropstrict Multiname("TestLoopInLoop",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestLoopInLoop")
            returnvoid
         end ; code
      end ; body
   end ; method
   
