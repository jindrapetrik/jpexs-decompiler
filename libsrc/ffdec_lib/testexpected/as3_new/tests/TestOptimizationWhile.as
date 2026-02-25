package tests
{
   public class TestOptimizationWhile
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
      
      public function TestOptimizationWhile()
      {
         method
            name "tests:TestOptimizationWhile/TestOptimizationWhile"
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
               name "tests:TestOptimizationWhile/run"
               returns null
               
               body
                  maxstack 3
                  localcount 5
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     debug 1, "b", 1, 14
                     debug 1, "c", 2, 15
                     debug 1, "d", 3, 16
                     pushbyte 1
                     convert_i
                     setlocal1
                     pushbyte 2
                     convert_i
                     setlocal2
                     pushbyte 3
                     convert_i
                     setlocal3
                     pushbyte 4
                     convert_i
                     setlocal 4
                     jump ofs0051
            ofs002b:
                     label
                     getlex QName(PackageNamespace(""),"Math")
                     getlex QName(PackageNamespace(""),"Math")
                     callproperty QName(PackageNamespace(""),"random"), 0
                     pushbyte 10
                     multiply
                     callproperty QName(PackageNamespace(""),"round"), 1
                     convert_i
                     setlocal 4
                     getlocal 4
                     pushbyte 10
                     ifnge ofs0048
                     jump ofs0056
            ofs0048:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestOptimizationWhile"),ProtectedNamespace("tests:TestOptimizationWhile"),StaticProtectedNs("tests:TestOptimizationWhile"),PrivateNamespace("TestOptimizationWhile.as$0")])
                     pushstring "xxx"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestOptimizationWhile"),ProtectedNamespace("tests:TestOptimizationWhile"),StaticProtectedNs("tests:TestOptimizationWhile"),PrivateNamespace("TestOptimizationWhile.as$0")]), 1
                     inclocal_i 4
            ofs0051:
                     pushtrue
                     iftrue ofs002b
            ofs0056:
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
            findpropstrict Multiname("TestOptimizationWhile",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestOptimizationWhile")
            returnvoid
         end ; code
      end ; body
   end ; method
   
