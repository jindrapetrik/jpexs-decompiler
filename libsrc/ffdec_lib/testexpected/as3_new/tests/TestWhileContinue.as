package tests
{
   public class TestWhileContinue
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
      
      public function TestWhileContinue()
      {
         method
            name "tests:TestWhileContinue/TestWhileContinue"
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
               name "tests:TestWhileContinue/run"
               returns null
               
               body
                  maxstack 2
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     pushbyte 5
                     coerce_a
                     setlocal1
                     jump ofs003b
            ofs000f:
                     label
                     getlocal1
                     pushbyte 9
                     ifne ofs0034
                     getlocal1
                     pushbyte 8
                     ifne ofs0022
                     jump ofs003b
            ofs0022:
                     getlocal1
                     pushbyte 9
                     ifne ofs002d
                     jump ofs0040
            ofs002d:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileContinue"),ProtectedNamespace("tests:TestWhileContinue"),StaticProtectedNs("tests:TestWhileContinue"),PrivateNamespace("TestWhileContinue.as$0")])
                     pushstring "hello 1"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileContinue"),ProtectedNamespace("tests:TestWhileContinue"),StaticProtectedNs("tests:TestWhileContinue"),PrivateNamespace("TestWhileContinue.as$0")]), 1
            ofs0034:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileContinue"),ProtectedNamespace("tests:TestWhileContinue"),StaticProtectedNs("tests:TestWhileContinue"),PrivateNamespace("TestWhileContinue.as$0")])
                     pushstring "hello2"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileContinue"),ProtectedNamespace("tests:TestWhileContinue"),StaticProtectedNs("tests:TestWhileContinue"),PrivateNamespace("TestWhileContinue.as$0")]), 1
            ofs003b:
                     pushtrue
                     iftrue ofs000f
            ofs0040:
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
            findpropstrict Multiname("TestWhileContinue",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestWhileContinue")
            returnvoid
         end ; code
      end ; body
   end ; method
   
