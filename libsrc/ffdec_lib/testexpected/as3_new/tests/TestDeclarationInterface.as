package tests
{
   public class TestDeclarationInterface
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
      
      public function TestDeclarationInterface()
      {
         method
            name "tests:TestDeclarationInterface/TestDeclarationInterface"
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
      
      public function run() : MyIFace
      {
         trait method QName(PackageNamespace(""),"run")
            dispid 0
            method
               name "tests:TestDeclarationInterface/run"
               returns QName(PrivateNamespace("TestDeclarationInterface.as$0"),"MyIFace")
               
               body
                  maxstack 2
                  localcount 4
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "i", 0, 13
                     debug 1, "n", 1, 14
                     pushnull
                     coerce QName(PrivateNamespace("TestDeclarationInterface.as$0"),"MyIFace")
                     setlocal1
                     pushbyte 2
                     convert_i
                     setlocal2
                     jump ofs0033
            ofs0018:
                     label
                     findpropstrict QName(PrivateNamespace("TestDeclarationInterface.as$0"),"MyClass")
                     constructprop QName(PrivateNamespace("TestDeclarationInterface.as$0"),"MyClass"), 0
                     coerce QName(PrivateNamespace("TestDeclarationInterface.as$0"),"MyIFace")
                     setlocal1
                     jump ofs005e
            ofs0025:
                     label
                     findpropstrict QName(PrivateNamespace("TestDeclarationInterface.as$0"),"MyClass2")
                     constructprop QName(PrivateNamespace("TestDeclarationInterface.as$0"),"MyClass2"), 0
                     coerce QName(PrivateNamespace("TestDeclarationInterface.as$0"),"MyIFace")
                     setlocal1
            ofs002e:
                     label
                     jump ofs005e
            ofs0033:
                     getlocal2
                     setlocal3
                     pushbyte 0
                     getlocal3
                     ifstrictne ofs0042
                     pushbyte 0
                     jump ofs0051
            ofs0042:
                     pushbyte 1
                     getlocal3
                     ifstrictne ofs004f
                     pushbyte 1
                     jump ofs0051
            ofs004f:
                     pushbyte -1
            ofs0051:
                     kill 3
                     lookupswitch ofs002e, [ofs0018, ofs0025]
            ofs005e:
                     getlocal1
                     returnvalue
                  end ; code
               end ; body
            end ; method
         }
      }
   }
   
   interface MyIFace
   {
      
      method
         name ""
         returns null
         
         body
            maxstack 1
            localcount 1
            initscopedepth 2
            maxscopedepth 3
            
            code
               getlocal0
               pushscope
               returnvoid
            end ; code
         end ; body
      end ; method
   }
   
   class MyClass implements MyIFace
   {
      
      method
         name ""
         returns null
         
         body
            maxstack 1
            localcount 1
            initscopedepth 4
            maxscopedepth 5
            
            code
               getlocal0
               pushscope
               returnvoid
            end ; code
         end ; body
      end ; method
      
      public function MyClass()
      {
         method
            name "TestDeclarationInterface.as$0:MyClass/MyClass"
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
   
   class MyClass2 implements MyIFace
   {
      
      method
         name ""
         returns null
         
         body
            maxstack 1
            localcount 1
            initscopedepth 4
            maxscopedepth 5
            
            code
               getlocal0
               pushscope
               returnvoid
            end ; code
         end ; body
      end ; method
      
      public function MyClass2()
      {
         method
            name "TestDeclarationInterface.as$0:MyClass2/MyClass2"
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
            findpropstrict Multiname("TestDeclarationInterface",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestDeclarationInterface")
            findpropstrict Multiname("MyIFace",[PrivateNamespace("TestDeclarationInterface.as$0")])
            pushnull
            newclass 1
            initproperty QName(PrivateNamespace("TestDeclarationInterface.as$0"),"MyIFace")
            findpropstrict Multiname("MyClass",[PrivateNamespace("TestDeclarationInterface.as$0")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 2
            popscope
            initproperty QName(PrivateNamespace("TestDeclarationInterface.as$0"),"MyClass")
            findpropstrict Multiname("MyClass2",[PrivateNamespace("TestDeclarationInterface.as$0")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 3
            popscope
            initproperty QName(PrivateNamespace("TestDeclarationInterface.as$0"),"MyClass2")
            returnvoid
         end ; code
      end ; body
   end ; method
   
