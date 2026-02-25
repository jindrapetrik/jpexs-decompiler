package tests
{
   import flash.utils.getDefinitionByName;
   
   public class TestOptimizationAndOr
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
      
      public function TestOptimizationAndOr()
      {
         method
            name "tests:TestOptimizationAndOr/TestOptimizationAndOr"
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
               name "tests:TestOptimizationAndOr/run"
               returns null
               
               body
                  maxstack 6
                  localcount 4
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "plugin", 0, 15
                     debug 1, "o", 1, 20
                     debug 1, "a", 2, 21
                     pushnull
                     coerce QName(PackageNamespace(""),"Object")
                     setlocal1
                     pushstring "a"
                     pushstring "Object"
                     pushstring "b"
                     pushstring "Object"
                     pushstring "c"
                     pushstring "Object"
                     newobject 3
                     coerce QName(PackageNamespace(""),"Object")
                     setlocal2
                     pushstring "d"
                     coerce_s
                     setlocal3
                     getlocal3
                     getlocal2
                     in
                     dup
                     iffalse ofs0044
                     pop
                     getlocal2
                     getlocal3
                     constructprop MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestOptimizationAndOr"),ProtectedNamespace("tests:TestOptimizationAndOr"),StaticProtectedNs("tests:TestOptimizationAndOr"),PrivateNamespace("TestOptimizationAndOr.as$0")]), 0
                     coerce QName(PackageNamespace(""),"Object")
                     dup
                     setlocal1
                     callproperty Multiname("toString",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestOptimizationAndOr"),ProtectedNamespace("tests:TestOptimizationAndOr"),StaticProtectedNs("tests:TestOptimizationAndOr"),PrivateNamespace("TestOptimizationAndOr.as$0")]), 0
                     getproperty Multiname("length",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestOptimizationAndOr"),ProtectedNamespace("tests:TestOptimizationAndOr"),StaticProtectedNs("tests:TestOptimizationAndOr"),PrivateNamespace("TestOptimizationAndOr.as$0")])
                     pushbyte 2
                     greaterthan
            ofs0044:
                     iffalse ofs004f
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestOptimizationAndOr"),ProtectedNamespace("tests:TestOptimizationAndOr"),StaticProtectedNs("tests:TestOptimizationAndOr"),PrivateNamespace("TestOptimizationAndOr.as$0")])
                     pushstring "okay"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestOptimizationAndOr"),ProtectedNamespace("tests:TestOptimizationAndOr"),StaticProtectedNs("tests:TestOptimizationAndOr"),PrivateNamespace("TestOptimizationAndOr.as$0")]), 1
            ofs004f:
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
            findpropstrict Multiname("TestOptimizationAndOr",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestOptimizationAndOr")
            returnvoid
         end ; code
      end ; body
   end ; method
   
