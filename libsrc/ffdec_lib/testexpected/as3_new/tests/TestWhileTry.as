package tests
{
   import flash.errors.EOFError;
   
   public class TestWhileTry
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
      
      public function TestWhileTry()
      {
         method
            name "tests:TestWhileTry/TestWhileTry"
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
               name "tests:TestWhileTry/run"
               flag NEED_ACTIVATION
               returns null
               
               body
                  maxstack 3
                  localcount 3
                  initscopedepth 5
                  maxscopedepth 12
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "+$activation", 0, 0
                     newactivation
                     dup
                     setlocal1
                     pushscope
                     jump ofs0049
            ofs000f:
                     label
            ofs0010:
                     jump ofs001c
            ofs0014:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileTry"),ProtectedNamespace("tests:TestWhileTry"),StaticProtectedNs("tests:TestWhileTry"),PrivateNamespace("TestWhileTry.as$0")])
                     pushstring "a"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestWhileTry"),ProtectedNamespace("tests:TestWhileTry"),StaticProtectedNs("tests:TestWhileTry"),PrivateNamespace("TestWhileTry.as$0")]), 1
            ofs001c:
                     pushtrue
                     iftrue ofs0014
            ofs0021:
                     jump ofs0049
            ofs0025:
                     getlocal0
                     pushscope
                     getlocal1
                     pushscope
                     newcatch 0
                     dup
                     setlocal2
                     dup
                     pushscope
                     swap
                     setslot 1
                     popscope
                     kill 2
                     jump ofs0049
            ofs0039:
                     getlocal0
                     pushscope
                     getlocal1
                     pushscope
                     newcatch 1
                     dup
                     setlocal2
                     dup
                     pushscope
                     swap
                     setslot 1
                     popscope
                     kill 2
            ofs0049:
                     pushtrue
                     iftrue ofs000f
                     returnvoid
                  end ; code
                  try from ofs0010 to ofs0021 target ofs0025 type QName(PackageNamespace("flash.errors"),"EOFError") name QName(PackageNamespace(""),"e") end
                  try from ofs0010 to ofs0021 target ofs0039 type QName(PackageNamespace(""),"Error") name QName(PackageNamespace(""),"e") end
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
            findpropstrict Multiname("TestWhileTry",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestWhileTry")
            returnvoid
         end ; code
      end ; body
   end ; method
   
