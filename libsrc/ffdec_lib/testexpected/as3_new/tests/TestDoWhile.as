package tests
{
   public class TestDoWhile
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
      
      public function TestDoWhile()
      {
         method
            name "tests:TestDoWhile/TestDoWhile"
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
               name "tests:TestDoWhile/run"
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
                     pushbyte 8
                     coerce_a
                     setlocal1
                     jump ofs0010
            ofs000f:
                     label
            ofs0010:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhile"),ProtectedNamespace("tests:TestDoWhile"),StaticProtectedNs("tests:TestDoWhile"),PrivateNamespace("TestDoWhile.as$0")])
                     pushstring "a="
                     getlocal1
                     add
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhile"),ProtectedNamespace("tests:TestDoWhile"),StaticProtectedNs("tests:TestDoWhile"),PrivateNamespace("TestDoWhile.as$0")]), 1
                     inclocal 1
                     getlocal1
                     pushbyte 20
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
            findpropstrict Multiname("TestDoWhile",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestDoWhile")
            returnvoid
         end ; code
      end ; body
   end ; method
   
