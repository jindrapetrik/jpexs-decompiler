package tests
{
   public class TestForEachReturn
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
      
      public function TestForEachReturn()
      {
         method
            name "tests:TestForEachReturn/TestForEachReturn"
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
               name "tests:TestForEachReturn/run"
               returns null
               
               body
                  maxstack 3
                  localcount 7
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "list", 0, 13
                     debug 1, "item", 1, 14
                     debug 1, "_loc3_", 2, 19
                     debug 1, "_loc4_", 3, 20
                     pushnull
                     coerce QName(PackageNamespace(""),"Array")
                     setlocal1
                     pushundefined
                     coerce_a
                     setlocal2
                     findpropstrict QName(PackageNamespace(""),"Array")
                     constructprop QName(PackageNamespace(""),"Array"), 0
                     coerce QName(PackageNamespace(""),"Array")
                     setlocal1
                     getlocal1
                     pushbyte 0
                     pushstring "first"
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachReturn"),ProtectedNamespace("tests:TestForEachReturn"),StaticProtectedNs("tests:TestForEachReturn"),PrivateNamespace("TestForEachReturn.as$0")])
                     getlocal1
                     pushbyte 1
                     pushstring "second"
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachReturn"),ProtectedNamespace("tests:TestForEachReturn"),StaticProtectedNs("tests:TestForEachReturn"),PrivateNamespace("TestForEachReturn.as$0")])
                     getlocal1
                     pushbyte 2
                     pushstring "third"
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForEachReturn"),ProtectedNamespace("tests:TestForEachReturn"),StaticProtectedNs("tests:TestForEachReturn"),PrivateNamespace("TestForEachReturn.as$0")])
                     pushbyte 0
                     convert_i
                     setlocal3
                     getlocal1
                     coerce_a
                     setlocal 4
                     pushbyte 0
                     setlocal 5
                     getlocal 4
                     coerce_a
                     setlocal 6
                     jump ofs0059
            ofs004f:
                     label
                     getlocal 6
                     getlocal 5
                     nextvalue
                     coerce_a
                     setlocal2
                     getlocal2
                     returnvalue
            ofs0059:
                     hasnext2 6, 5
                     iftrue ofs004f
                     kill 6
                     kill 5
                     pushnull
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
            findpropstrict Multiname("TestForEachReturn",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestForEachReturn")
            returnvoid
         end ; code
      end ; body
   end ; method
   
