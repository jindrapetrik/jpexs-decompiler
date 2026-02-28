package tests_classes.mypackage1
{
   import tests_classes.mypackage2.TestClass;
   import tests_classes.mypackage2.TestInterface;
   
   public class TestClass implements tests_classes.mypackage1.TestInterface
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
      
      public function TestClass()
      {
         method
            name "tests_classes.mypackage1:TestClass/TestClass"
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
      
      public function testCall() : String
      {
         trait method QName(PackageNamespace(""),"testCall")
            dispid 0
            method
               name "tests_classes.mypackage1:TestClass/testCall"
               returns QName(PackageNamespace(""),"String")
               
               body
                  maxstack 2
                  localcount 1
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes.mypackage1"),PackageInternalNs("tests_classes.mypackage1"),PrivateNamespace("tests_classes.mypackage1:TestClass"),ProtectedNamespace("tests_classes.mypackage1:TestClass"),StaticProtectedNs("tests_classes.mypackage1:TestClass"),PrivateNamespace("TestClass.as$0")])
                     pushstring "pkg1hello"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes.mypackage1"),PackageInternalNs("tests_classes.mypackage1"),PrivateNamespace("tests_classes.mypackage1:TestClass"),ProtectedNamespace("tests_classes.mypackage1:TestClass"),StaticProtectedNs("tests_classes.mypackage1:TestClass"),PrivateNamespace("TestClass.as$0")]), 1
                     pushstring "pkg1hello"
                     returnvalue
                  end ; code
               end ; body
            end ; method
         }
         
         public function testMethod1() : void
         {
            trait method QName(PackageNamespace(""),"testMethod1")
               dispid 0
               method
                  name "tests_classes.mypackage1:TestClass/testMethod1"
                  returns QName(PackageNamespace(""),"void")
                  
                  body
                     maxstack 1
                     localcount 3
                     initscopedepth 4
                     maxscopedepth 5
                     
                     code
                        getlocal0
                        pushscope
                        debug 1, "a", 0, 22
                        debug 1, "b", 1, 24
                        getlocal0
                        coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
                        setlocal1
                        getlocal1
                        callpropvoid QName(Namespace("tests_classes.mypackage1:TestInterface"),"testMethod1"), 0
                        getlocal0
                        coerce QName(PackageNamespace("tests_classes.mypackage2"),"TestInterface")
                        setlocal2
                        findpropstrict QName(PackageNamespace("tests_classes.mypackage2"),"TestClass")
                        constructprop QName(PackageNamespace("tests_classes.mypackage2"),"TestClass"), 0
                        coerce QName(PackageNamespace("tests_classes.mypackage2"),"TestInterface")
                        setlocal2
                        returnvoid
                     end ; code
                  end ; body
               end ; method
            }
            
            public function testMethod2() : void
            {
               trait method QName(PackageNamespace(""),"testMethod2")
                  dispid 0
                  method
                     name "tests_classes.mypackage1:TestClass/testMethod2"
                     returns QName(PackageNamespace(""),"void")
                     
                     body
                        maxstack 1
                        localcount 3
                        initscopedepth 4
                        maxscopedepth 5
                        
                        code
                           getlocal0
                           pushscope
                           debug 1, "a", 0, 30
                           debug 1, "b", 1, 32
                           getlocal0
                           coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
                           setlocal1
                           getlocal1
                           callpropvoid QName(Namespace("tests_classes.mypackage1:TestInterface"),"testMethod1"), 0
                           getlocal0
                           coerce QName(PackageNamespace("tests_classes.mypackage2"),"TestInterface")
                           setlocal2
                           findpropstrict QName(PackageNamespace("tests_classes.mypackage2"),"TestClass")
                           constructprop QName(PackageNamespace("tests_classes.mypackage2"),"TestClass"), 0
                           coerce QName(PackageNamespace("tests_classes.mypackage2"),"TestInterface")
                           setlocal2
                           returnvoid
                        end ; code
                     end ; body
                  end ; method
               }
               
               public function testParam(p1:tests_classes.mypackage1.TestInterface, p2:tests_classes.mypackage2.TestInterface) : void
               {
                  trait method QName(PackageNamespace(""),"testParam")
                     dispid 0
                     method
                        name "tests_classes.mypackage1:TestClass/testParam"
                        flag NEED_ACTIVATION
                        param QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
                        param QName(PackageNamespace("tests_classes.mypackage2"),"TestInterface")
                        returns QName(PackageNamespace(""),"void")
                        
                        body
                           maxstack 2
                           localcount 4
                           initscopedepth 5
                           maxscopedepth 7
                           trait slot QName(PackageInternalNs("tests_classes.mypackage1"),"p1")
                              slotid 1
                              type QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
                           end ; trait
                           trait slot QName(PackageInternalNs("tests_classes.mypackage1"),"p2")
                              slotid 2
                              type QName(PackageNamespace("tests_classes.mypackage2"),"TestInterface")
                           end ; trait
                           trait slot QName(PackageInternalNs("tests_classes.mypackage1"),"m")
                              slotid 3
                              type QName(PackageNamespace(""),"Function")
                           end ; trait
                           
                           code
                              getlocal0
                              pushscope
                              debug 1, "p1", 0, 0
                              debug 1, "p2", 1, 0
                              debug 1, "+$activation", 2, 0
                              newactivation
                              dup
                              setlocal3
                              pushscope
                              getscopeobject 1
                              getlocal1
                              coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestInterface")
                              setslot 1
                              getscopeobject 1
                              getlocal2
                              coerce QName(PackageNamespace("tests_classes.mypackage2"),"TestInterface")
                              setslot 2
                              getscopeobject 1
                              newfunction 5
                              coerce QName(PackageNamespace(""),"Function")
                              setslot 3
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
                     findpropstrict Multiname("TestClass",[PackageNamespace("tests_classes.mypackage1")])
                     getlex QName(PackageNamespace(""),"Object")
                     pushscope
                     getlex QName(PackageNamespace(""),"Object")
                     newclass 0
                     popscope
                     initproperty QName(PackageNamespace("tests_classes.mypackage1"),"TestClass")
                     returnvoid
                  end ; code
               end ; body
            end ; method
            
