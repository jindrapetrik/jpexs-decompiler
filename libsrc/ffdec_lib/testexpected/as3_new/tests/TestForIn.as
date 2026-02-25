package tests
{
   import flash.utils.Dictionary;
   
   public class TestForIn
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
      
      public function TestForIn()
      {
         method
            name "tests:TestForIn/TestForIn"
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
               name "tests:TestForIn/run"
               returns null
               
               body
                  maxstack 2
                  localcount 5
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "dic", 0, 15
                     debug 1, "item", 1, 16
                     pushnull
                     coerce QName(PackageNamespace("flash.utils"),"Dictionary")
                     setlocal1
                     pushnull
                     coerce_a
                     setlocal2
                     pushbyte 0
                     setlocal3
                     getlocal1
                     coerce_a
                     setlocal 4
                     jump ofs002b
            ofs001e:
                     label
                     getlocal 4
                     getlocal3
                     nextname
                     coerce_a
                     setlocal2
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForIn"),ProtectedNamespace("tests:TestForIn"),StaticProtectedNs("tests:TestForIn"),PrivateNamespace("TestForIn.as$0")])
                     getlocal2
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForIn"),ProtectedNamespace("tests:TestForIn"),StaticProtectedNs("tests:TestForIn"),PrivateNamespace("TestForIn.as$0")]), 1
            ofs002b:
                     hasnext2 4, 3
                     iftrue ofs001e
                     kill 4
                     kill 3
                     pushbyte 0
                     setlocal3
                     getlocal1
                     coerce_a
                     setlocal 4
                     jump ofs004e
            ofs0041:
                     label
                     getlocal 4
                     getlocal3
                     nextvalue
                     coerce_a
                     setlocal2
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForIn"),ProtectedNamespace("tests:TestForIn"),StaticProtectedNs("tests:TestForIn"),PrivateNamespace("TestForIn.as$0")])
                     getlocal2
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForIn"),ProtectedNamespace("tests:TestForIn"),StaticProtectedNs("tests:TestForIn"),PrivateNamespace("TestForIn.as$0")]), 1
            ofs004e:
                     hasnext2 4, 3
                     iftrue ofs0041
                     kill 4
                     kill 3
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
            findpropstrict Multiname("TestForIn",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestForIn")
            returnvoid
         end ; code
      end ; body
   end ; method
   
