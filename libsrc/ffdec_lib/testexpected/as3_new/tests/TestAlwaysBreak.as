package tests
{
   public class TestAlwaysBreak
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
      
      public function TestAlwaysBreak()
      {
         method
            name "tests:TestAlwaysBreak/TestAlwaysBreak"
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
               name "tests:TestAlwaysBreak/run"
               returns null
               
               body
                  maxstack 2
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "v", 0, 13
                     pushbyte 0
                     convert_i
                     setlocal1
                     pushbyte 5
                     convert_i
                     setlocal1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestAlwaysBreak"),ProtectedNamespace("tests:TestAlwaysBreak"),StaticProtectedNs("tests:TestAlwaysBreak"),PrivateNamespace("TestAlwaysBreak.as$0")])
                     pushstring "a"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestAlwaysBreak"),ProtectedNamespace("tests:TestAlwaysBreak"),StaticProtectedNs("tests:TestAlwaysBreak"),PrivateNamespace("TestAlwaysBreak.as$0")]), 1
                     jump ofs004d
            ofs001a:
                     label
                     getlocal1
                     pushbyte 4
                     ifngt ofs0042
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestAlwaysBreak"),ProtectedNamespace("tests:TestAlwaysBreak"),StaticProtectedNs("tests:TestAlwaysBreak"),PrivateNamespace("TestAlwaysBreak.as$0")])
                     pushstring "b"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestAlwaysBreak"),ProtectedNamespace("tests:TestAlwaysBreak"),StaticProtectedNs("tests:TestAlwaysBreak"),PrivateNamespace("TestAlwaysBreak.as$0")]), 1
                     getlocal1
                     pushbyte 10
                     ifngt ofs003b
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestAlwaysBreak"),ProtectedNamespace("tests:TestAlwaysBreak"),StaticProtectedNs("tests:TestAlwaysBreak"),PrivateNamespace("TestAlwaysBreak.as$0")])
                     pushstring "c"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestAlwaysBreak"),ProtectedNamespace("tests:TestAlwaysBreak"),StaticProtectedNs("tests:TestAlwaysBreak"),PrivateNamespace("TestAlwaysBreak.as$0")]), 1
                     jump ofs0052
            ofs003b:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestAlwaysBreak"),ProtectedNamespace("tests:TestAlwaysBreak"),StaticProtectedNs("tests:TestAlwaysBreak"),PrivateNamespace("TestAlwaysBreak.as$0")])
                     pushstring "d"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestAlwaysBreak"),ProtectedNamespace("tests:TestAlwaysBreak"),StaticProtectedNs("tests:TestAlwaysBreak"),PrivateNamespace("TestAlwaysBreak.as$0")]), 1
            ofs0042:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestAlwaysBreak"),ProtectedNamespace("tests:TestAlwaysBreak"),StaticProtectedNs("tests:TestAlwaysBreak"),PrivateNamespace("TestAlwaysBreak.as$0")])
                     pushstring "e"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestAlwaysBreak"),ProtectedNamespace("tests:TestAlwaysBreak"),StaticProtectedNs("tests:TestAlwaysBreak"),PrivateNamespace("TestAlwaysBreak.as$0")]), 1
                     jump ofs0052
            ofs004d:
                     pushtrue
                     iftrue ofs001a
            ofs0052:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestAlwaysBreak"),ProtectedNamespace("tests:TestAlwaysBreak"),StaticProtectedNs("tests:TestAlwaysBreak"),PrivateNamespace("TestAlwaysBreak.as$0")])
                     pushstring "f"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestAlwaysBreak"),ProtectedNamespace("tests:TestAlwaysBreak"),StaticProtectedNs("tests:TestAlwaysBreak"),PrivateNamespace("TestAlwaysBreak.as$0")]), 1
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
            findpropstrict Multiname("TestAlwaysBreak",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestAlwaysBreak")
            returnvoid
         end ; code
      end ; body
   end ; method
   
