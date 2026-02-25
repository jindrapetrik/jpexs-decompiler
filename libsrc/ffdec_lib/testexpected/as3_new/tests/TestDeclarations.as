package tests
{
   public class TestDeclarations
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
      
      public function TestDeclarations()
      {
         method
            name "tests:TestDeclarations/TestDeclarations"
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
               name "tests:TestDeclarations/run"
               returns null
               
               body
                  maxstack 1
                  localcount 8
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "vall", 0, 13
                     debug 1, "vstr", 1, 14
                     debug 1, "vint", 2, 15
                     debug 1, "vuint", 3, 16
                     debug 1, "vclass", 4, 17
                     debug 1, "vnumber", 5, 18
                     debug 1, "vobject", 6, 19
                     pushundefined
                     coerce_a
                     setlocal1
                     pushnull
                     coerce_s
                     setlocal2
                     pushbyte 0
                     convert_i
                     setlocal3
                     pushbyte 0
                     convert_u
                     setlocal 4
                     pushnull
                     coerce QName(PrivateNamespace("TestDeclarations.as$0"),"TestClass1")
                     setlocal 5
                     getlex Multiname("NaN",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDeclarations"),ProtectedNamespace("tests:TestDeclarations"),StaticProtectedNs("tests:TestDeclarations"),PrivateNamespace("TestDeclarations.as$0")])
                     convert_d
                     setlocal 6
                     pushnull
                     coerce QName(PackageNamespace(""),"Object")
                     setlocal 7
                     pushbyte 6
                     coerce_a
                     setlocal1
                     pushstring "hello"
                     coerce_s
                     setlocal2
                     pushbyte 7
                     convert_u
                     setlocal 4
                     pushbyte -4
                     convert_i
                     setlocal3
                     findpropstrict QName(PrivateNamespace("TestDeclarations.as$0"),"TestClass1")
                     constructprop QName(PrivateNamespace("TestDeclarations.as$0"),"TestClass1"), 0
                     coerce QName(PrivateNamespace("TestDeclarations.as$0"),"TestClass1")
                     setlocal 5
                     pushdouble 0.5
                     convert_d
                     setlocal 6
                     pushbyte 6
                     convert_d
                     setlocal 6
                     getlocal 5
                     coerce QName(PackageNamespace(""),"Object")
                     setlocal 7
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
      }
   }
   
   class TestClass1
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
      
      public function TestClass1()
      {
         method
            name "TestDeclarations.as$0:TestClass1/TestClass1"
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
            findpropstrict Multiname("TestDeclarations",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestDeclarations")
            findpropstrict Multiname("TestClass1",[PrivateNamespace("TestDeclarations.as$0")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 1
            popscope
            initproperty QName(PrivateNamespace("TestDeclarations.as$0"),"TestClass1")
            returnvoid
         end ; code
      end ; body
   end ; method
   
