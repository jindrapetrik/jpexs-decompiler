package tests
{
   public class TestGotos2
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
      
      public function TestGotos2()
      {
         method
            name "tests:TestGotos2/TestGotos2"
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
      
      public function run() : int
      {
         trait method QName(PackageNamespace(""),"run")
            dispid 0
            method
               name "tests:TestGotos2/run"
               returns QName(PackageNamespace(""),"int")
               
               body
                  maxstack 2
                  localcount 4
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     debug 1, "b", 1, 14
                     debug 1, "c", 2, 15
                     pushtrue
                     convert_b
                     setlocal1
                     pushfalse
                     convert_b
                     setlocal2
                     pushtrue
                     convert_b
                     setlocal3
                     getlocal1
                     iffalse ofs003b
                     getlocal2
                     iffalse ofs0037
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos2"),ProtectedNamespace("tests:TestGotos2"),StaticProtectedNs("tests:TestGotos2"),PrivateNamespace("TestGotos2.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos2"),ProtectedNamespace("tests:TestGotos2"),StaticProtectedNs("tests:TestGotos2"),PrivateNamespace("TestGotos2.as$0")]), 1
                     getlocal3
                     iffalse ofs0037
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos2"),ProtectedNamespace("tests:TestGotos2"),StaticProtectedNs("tests:TestGotos2"),PrivateNamespace("TestGotos2.as$0")])
                     pushstring "B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos2"),ProtectedNamespace("tests:TestGotos2"),StaticProtectedNs("tests:TestGotos2"),PrivateNamespace("TestGotos2.as$0")]), 1
            ofs0037:
                     jump ofs0042
            ofs003b:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos2"),ProtectedNamespace("tests:TestGotos2"),StaticProtectedNs("tests:TestGotos2"),PrivateNamespace("TestGotos2.as$0")])
                     pushstring "E"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos2"),ProtectedNamespace("tests:TestGotos2"),StaticProtectedNs("tests:TestGotos2"),PrivateNamespace("TestGotos2.as$0")]), 1
            ofs0042:
                     pushbyte 5
                     returnvalue
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
            findpropstrict Multiname("TestGotos2",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestGotos2")
            returnvoid
         end ; code
      end ; body
   end ; method
   
