package tests
{
   import tests_classes.mypackage1.TestClass;
   import tests_classes.mypackage1.TestInterface;
   
   public class TestAndOrCoercion
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
      
      private var ti:TestInterface;
      
      private var tc:TestClass;
      
      private var i:int = 5;
      
      private var j:int = 6;
      
      private var tx:TestInterface;
      
      public function TestAndOrCoercion()
      {
         method
            name "tests:TestAndOrCoercion/TestAndOrCoercion"
            returns null
            
            body
               maxstack 3
               localcount 1
               initscopedepth 4
               maxscopedepth 5
               
               code
                  getlocal0
                  pushscope
                  getlocal0
                  getlocal0
                  getproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"ti")
                  coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
                  dup
                  convert_b
                  iftrue ofs0014
                  pop
                  getlocal0
                  getproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"tc")
                  coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
         ofs0014:
                  setproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"tx")
                  getlocal0
                  constructsuper 0
                  returnvoid
               end ; code
            end ; body
         end ; method
      }
      
      public function run() : TestInterface
      {
         trait method QName(PackageNamespace(""),"run")
            dispid 0
            method
               name "tests:TestAndOrCoercion/run"
               returns QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
               
               body
                  maxstack 4
                  localcount 7
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "x", 0, 26
                     debug 1, "y", 1, 27
                     debug 1, "z", 2, 28
                     debug 1, "a", 3, 30
                     debug 1, "b", 4, 31
                     getlocal0
                     getproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"ti")
                     coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
                     dup
                     convert_b
                     iftrue ofs0050
                     pop
                     getlocal0
                     findpropstrict QName(PackageNamespace("tests_classes.mypackage1"),"TestClass")
                     constructprop QName(PackageNamespace("tests_classes.mypackage1"),"TestClass"), 0
                     dup
                     setlocal 6
                     setproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"ti")
                     getlocal 6
                     kill 6
                     coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
                     dup
                     convert_b
                     iffalse ofs0050
                     pop
                     getlocal0
                     findpropstrict QName(PackageNamespace("tests_classes.mypackage1"),"TestClass")
                     constructprop QName(PackageNamespace("tests_classes.mypackage1"),"TestClass"), 0
                     dup
                     setlocal 6
                     setproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"ti")
                     getlocal 6
                     kill 6
                     coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
            ofs0050:
                     coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
                     setlocal1
                     getlocal0
                     getproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"ti")
                     coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
                     dup
                     convert_b
                     iffalse ofs0070
                     pop
                     getlocal0
                     findpropstrict QName(PackageNamespace("tests_classes.mypackage1"),"TestClass")
                     constructprop QName(PackageNamespace("tests_classes.mypackage1"),"TestClass"), 0
                     dup
                     setlocal 6
                     setproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"ti")
                     getlocal 6
                     kill 6
                     coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
            ofs0070:
                     coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
                     setlocal2
                     getlocal0
                     getproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"tc")
                     coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestClass")
                     dup
                     convert_b
                     iftrue ofs0090
                     pop
                     getlocal0
                     findpropstrict QName(PackageNamespace("tests_classes.mypackage1"),"TestClass")
                     constructprop QName(PackageNamespace("tests_classes.mypackage1"),"TestClass"), 0
                     dup
                     setlocal 6
                     setproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"tc")
                     getlocal 6
                     kill 6
                     coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestClass")
            ofs0090:
                     coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestClass")
                     setlocal3
                     getlocal0
                     getlocal0
                     getproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"ti")
                     coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
                     dup
                     convert_b
                     iffalse ofs00b1
                     pop
                     getlocal0
                     findpropstrict QName(PackageNamespace("tests_classes.mypackage1"),"TestClass")
                     constructprop QName(PackageNamespace("tests_classes.mypackage1"),"TestClass"), 0
                     dup
                     setlocal 6
                     setproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"ti")
                     getlocal 6
                     kill 6
                     coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
            ofs00b1:
                     setproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"ti")
                     getlocal0
                     getproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"ti")
                     coerce_a
                     dup
                     convert_b
                     iffalse ofs00ce
                     pop
                     getlocal0
                     findpropstrict QName(PackageNamespace("tests_classes.mypackage1"),"TestClass")
                     constructprop QName(PackageNamespace("tests_classes.mypackage1"),"TestClass"), 0
                     dup
                     setlocal 6
                     setproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"ti")
                     getlocal 6
                     kill 6
                     coerce_a
            ofs00ce:
                     coerce_a
                     setlocal 4
                     pushbyte 1
                     getlocal0
                     getproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"i")
                     dup
                     convert_b
                     iftrue ofs00e0
                     pop
                     getlocal0
                     getproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"j")
            ofs00e0:
                     add
                     convert_i
                     setlocal 5
                     getlocal0
                     getlocal0
                     getproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"ti")
                     coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
                     dup
                     convert_b
                     iffalse ofs0102
                     pop
                     getlocal0
                     findpropstrict QName(PackageNamespace("tests_classes.mypackage1"),"TestClass")
                     constructprop QName(PackageNamespace("tests_classes.mypackage1"),"TestClass"), 0
                     dup
                     setlocal 6
                     setproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"ti")
                     getlocal 6
                     kill 6
                     coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
            ofs0102:
                     callpropvoid QName(PackageNamespace(""),"test"), 1
                     getlocal0
                     getproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"ti")
                     coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
                     dup
                     convert_b
                     iffalse ofs0122
                     pop
                     getlocal0
                     findpropstrict QName(PackageNamespace("tests_classes.mypackage1"),"TestClass")
                     constructprop QName(PackageNamespace("tests_classes.mypackage1"),"TestClass"), 0
                     dup
                     setlocal 6
                     setproperty QName(PrivateNamespace("tests:TestAndOrCoercion"),"ti")
                     getlocal 6
                     kill 6
                     coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
            ofs0122:
                     returnvalue
                  end ; code
               end ; body
            end ; method
         }
         
         public function test(p:TestInterface) : void
         {
            trait method QName(PackageNamespace(""),"test")
               dispid 0
               method
                  name "tests:TestAndOrCoercion/test"
                  param QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
                  returns QName(PackageNamespace(""),"void")
                  
                  body
                     maxstack 1
                     localcount 2
                     initscopedepth 4
                     maxscopedepth 5
                     
                     code
                        getlocal0
                        pushscope
                        debug 1, "p", 0, 0
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
               findpropstrict Multiname("TestAndOrCoercion",[PackageNamespace("tests")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 0
               popscope
               initproperty QName(PackageNamespace("tests"),"TestAndOrCoercion")
               returnvoid
            end ; code
         end ; body
      end ; method
      
