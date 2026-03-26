package tests
{
   public class TestAndInt
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
      
      public function TestAndInt()
      {
         method
            name "tests:TestAndInt/TestAndInt"
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
               name "tests:TestAndInt/run"
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
                     pushbyte 5
                     convert_i
                     setlocal2
                     pushbyte 0
                     convert_b
                     dup
                     convert_b
                     iffalse ofs002c
                     pop
                     pushbyte 1
                     convert_b
                     dup
                     convert_b
                     iftrue ofs002c
                     pop
                     getlocal1
                     getlocal2
                     lessthan
                     convert_b
            ofs002c:
                     iffalse ofs0037
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestAndInt"),ProtectedNamespace("tests:TestAndInt"),StaticProtectedNs("tests:TestAndInt"),PrivateNamespace("TestAndInt.as$0")])
                     pushstring "okay"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestAndInt"),ProtectedNamespace("tests:TestAndInt"),StaticProtectedNs("tests:TestAndInt"),PrivateNamespace("TestAndInt.as$0")]), 1
            ofs0037:
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
            findpropstrict Multiname("TestAndInt",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestAndInt")
            returnvoid
         end ; code
      end ; body
   end ; method
   
