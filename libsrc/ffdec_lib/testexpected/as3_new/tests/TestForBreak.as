package tests
{
   public class TestForBreak
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
      
      public function TestForBreak()
      {
         method
            name "tests:TestForBreak/TestForBreak"
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
               name "tests:TestForBreak/run"
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
                     jump ofs0026
            ofs000f:
                     label
                     getlocal1
                     pushbyte 5
                     ifne ofs001b
                     jump ofs002d
            ofs001b:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForBreak"),ProtectedNamespace("tests:TestForBreak"),StaticProtectedNs("tests:TestForBreak"),PrivateNamespace("TestForBreak.as$0")])
                     pushstring "hello:"
                     getlocal1
                     add
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForBreak"),ProtectedNamespace("tests:TestForBreak"),StaticProtectedNs("tests:TestForBreak"),PrivateNamespace("TestForBreak.as$0")]), 1
                     inclocal 1
            ofs0026:
                     getlocal1
                     pushbyte 10
                     iflt ofs000f
            ofs002d:
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
            findpropstrict Multiname("TestForBreak",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestForBreak")
            returnvoid
         end ; code
      end ; body
   end ; method
   
