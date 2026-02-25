package tests
{
   public class TestActivationArguments
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
      
      public function TestActivationArguments()
      {
         method
            name "tests:TestActivationArguments/TestActivationArguments"
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
               name "tests:TestActivationArguments/run"
               flag NEED_ACTIVATION
               flag NEED_ARGUMENTS
               returns null
               
               body
                  maxstack 3
                  localcount 3
                  initscopedepth 5
                  maxscopedepth 7
                  trait slot QName(PackageInternalNs("tests"),"arguments")
                     slotid 1
                     type QName(PackageNamespace(""),"Array")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"func")
                     slotid 2
                     type QName(PackageNamespace(""),"Function")
                  end ; trait
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "arguments", 0, 0
                     debug 1, "+$activation", 1, 0
                     newactivation
                     dup
                     setlocal2
                     pushscope
                     getscopeobject 1
                     getlocal1
                     coerce QName(PackageNamespace(""),"Array")
                     setslot 1
                     getscopeobject 1
                     newfunction 2
                     coerce QName(PackageNamespace(""),"Function")
                     setslot 2
                     getscopeobject 1
                     getslot 1
                     getproperty Multiname("length",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestActivationArguments"),ProtectedNamespace("tests:TestActivationArguments"),StaticProtectedNs("tests:TestActivationArguments"),PrivateNamespace("TestActivationArguments.as$0")])
                     pushbyte 0
                     ifngt ofs0038
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestActivationArguments"),ProtectedNamespace("tests:TestActivationArguments"),StaticProtectedNs("tests:TestActivationArguments"),PrivateNamespace("TestActivationArguments.as$0")])
                     getscopeobject 1
                     getslot 1
                     pushbyte 0
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestActivationArguments"),ProtectedNamespace("tests:TestActivationArguments"),StaticProtectedNs("tests:TestActivationArguments"),PrivateNamespace("TestActivationArguments.as$0")])
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestActivationArguments"),ProtectedNamespace("tests:TestActivationArguments"),StaticProtectedNs("tests:TestActivationArguments"),PrivateNamespace("TestActivationArguments.as$0")]), 1
            ofs0038:
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
            findpropstrict Multiname("TestActivationArguments",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestActivationArguments")
            returnvoid
         end ; code
      end ; body
   end ; method
   
