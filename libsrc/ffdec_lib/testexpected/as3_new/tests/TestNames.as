package tests
{
   import tests_other.myInternal;
   import tests_other.myInternal2;
   
   use namespace myInternal;
   use namespace myInternal2;
   
   public class TestNames
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
      
      myInternal var neco:int;
      
      myInternal2 var neco:int;
      
      internal var nic:int;
      
      public function TestNames()
      {
         method
            name "tests:TestNames/TestNames"
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
               name "tests:TestNames/run"
               returns null
               
               body
                  maxstack 3
                  localcount 8
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "ns", 0, 25
                     debug 1, "name", 1, 26
                     debug 1, "a", 2, 27
                     debug 1, "b", 3, 28
                     debug 1, "c", 4, 30
                     debug 1, "d", 5, 31
                     getlocal0
                     callproperty QName(PackageNamespace(""),"getNamespace"), 0
                     coerce_a
                     setlocal1
                     getlocal0
                     callproperty QName(PackageNamespace(""),"getName"), 0
                     coerce_a
                     setlocal2
                     getlocal1
                     coerce QName(PackageNamespace(""),"Namespace")
                     findpropstrict RTQName("unnamespacedFunc")
                     dup
                     setlocal 7
                     getlocal1
                     coerce QName(PackageNamespace(""),"Namespace")
                     getproperty RTQName("unnamespacedFunc")
                     getlocal 7
                     call 0
                     kill 7
                     coerce_a
                     setlocal3
                     getlocal1
                     coerce QName(PackageNamespace(""),"Namespace")
                     getlocal2
                     coerce_s
                     convert_s
                     findpropstrict RTQNameL()
                     getlocal1
                     coerce QName(PackageNamespace(""),"Namespace")
                     getlocal2
                     coerce_s
                     convert_s
                     getproperty RTQNameL()
                     coerce_a
                     setlocal 4
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),Namespace("http://www.adobe.com/2006/actionscript/examples"),PackageInternalNs("tests_other:myInternal2"),PrivateNamespace("tests:TestNames"),ProtectedNamespace("tests:TestNames"),StaticProtectedNs("tests:TestNames"),PrivateNamespace("TestNames.as$0")])
                     getlocal 4
                     getproperty Multiname("c",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),Namespace("http://www.adobe.com/2006/actionscript/examples"),PackageInternalNs("tests_other:myInternal2"),PrivateNamespace("tests:TestNames"),ProtectedNamespace("tests:TestNames"),StaticProtectedNs("tests:TestNames"),PrivateNamespace("TestNames.as$0")])
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),Namespace("http://www.adobe.com/2006/actionscript/examples"),PackageInternalNs("tests_other:myInternal2"),PrivateNamespace("tests:TestNames"),ProtectedNamespace("tests:TestNames"),StaticProtectedNs("tests:TestNames"),PrivateNamespace("TestNames.as$0")]), 1
                     findproperty QName(PackageNamespace("tests_other"),"myInternal")
                     getproperty QName(PackageNamespace("tests_other"),"myInternal")
                     coerce QName(PackageNamespace(""),"Namespace")
                     findpropstrict RTQName("neco")
                     findproperty QName(PackageNamespace("tests_other"),"myInternal")
                     getproperty QName(PackageNamespace("tests_other"),"myInternal")
                     coerce QName(PackageNamespace(""),"Namespace")
                     getproperty RTQName("neco")
                     coerce_a
                     setlocal 5
                     getlocal0
                     findproperty QName(PackageNamespace("tests_other"),"myInternal2")
                     getproperty QName(PackageNamespace("tests_other"),"myInternal2")
                     coerce QName(PackageNamespace(""),"Namespace")
                     getproperty RTQName("neco")
                     coerce_a
                     setlocal 6
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
         
         public function getNamespace() : Namespace
         {
            trait method QName(PackageNamespace(""),"getNamespace")
               dispid 0
               method
                  name "tests:TestNames/getNamespace"
                  returns QName(PackageNamespace(""),"Namespace")
                  
                  body
                     maxstack 1
                     localcount 1
                     initscopedepth 4
                     maxscopedepth 5
                     
                     code
                        getlocal0
                        pushscope
                        findproperty QName(PackageNamespace("tests_other"),"myInternal")
                        getproperty QName(PackageNamespace("tests_other"),"myInternal")
                        returnvalue
                     end ; code
                  end ; body
               end ; method
            }
            
            public function getName() : String
            {
               trait method QName(PackageNamespace(""),"getName")
                  dispid 0
                  method
                     name "tests:TestNames/getName"
                     returns QName(PackageNamespace(""),"String")
                     
                     body
                        maxstack 1
                        localcount 1
                        initscopedepth 4
                        maxscopedepth 5
                        
                        code
                           getlocal0
                           pushscope
                           pushstring "unnamespacedFunc"
                           returnvalue
                        end ; code
                     end ; body
                  end ; method
               }
               
               myInternal function namespacedFunc() : void
               {
                  trait method QName(Namespace("http://www.adobe.com/2006/actionscript/examples"),"namespacedFunc")
                     dispid 0
                     method
                        name "tests:TestNamesmyInternal/namespacedFunc"
                        returns QName(PackageNamespace(""),"void")
                        
                        body
                           maxstack 2
                           localcount 1
                           initscopedepth 4
                           maxscopedepth 5
                           
                           code
                              getlocal0
                              pushscope
                              findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),Namespace("http://www.adobe.com/2006/actionscript/examples"),PackageInternalNs("tests_other:myInternal2"),PrivateNamespace("tests:TestNames"),ProtectedNamespace("tests:TestNames"),StaticProtectedNs("tests:TestNames"),PrivateNamespace("TestNames.as$0")])
                              pushstring "hello"
                              callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),Namespace("http://www.adobe.com/2006/actionscript/examples"),PackageInternalNs("tests_other:myInternal2"),PrivateNamespace("tests:TestNames"),ProtectedNamespace("tests:TestNames"),StaticProtectedNs("tests:TestNames"),PrivateNamespace("TestNames.as$0")]), 1
                              returnvoid
                           end ; code
                        end ; body
                     end ; method
                  }
                  
                  myInternal2 function namespacedFunc2() : void
                  {
                     trait method QName(PackageInternalNs("tests_other:myInternal2"),"namespacedFunc2")
                        dispid 0
                        method
                           name "tests:TestNamesmyInternal2/namespacedFunc2"
                           returns QName(PackageNamespace(""),"void")
                           
                           body
                              maxstack 2
                              localcount 1
                              initscopedepth 4
                              maxscopedepth 5
                              
                              code
                                 getlocal0
                                 pushscope
                                 findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),Namespace("http://www.adobe.com/2006/actionscript/examples"),PackageInternalNs("tests_other:myInternal2"),PrivateNamespace("tests:TestNames"),ProtectedNamespace("tests:TestNames"),StaticProtectedNs("tests:TestNames"),PrivateNamespace("TestNames.as$0")])
                                 pushstring "hello"
                                 callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),Namespace("http://www.adobe.com/2006/actionscript/examples"),PackageInternalNs("tests_other:myInternal2"),PrivateNamespace("tests:TestNames"),ProtectedNamespace("tests:TestNames"),StaticProtectedNs("tests:TestNames"),PrivateNamespace("TestNames.as$0")]), 1
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
                        findpropstrict Multiname("TestNames",[PackageNamespace("tests")])
                        getlex QName(PackageNamespace(""),"Object")
                        pushscope
                        getlex QName(PackageNamespace(""),"Object")
                        newclass 0
                        popscope
                        initproperty QName(PackageNamespace("tests"),"TestNames")
                        returnvoid
                     end ; code
                  end ; body
               end ; method
               
