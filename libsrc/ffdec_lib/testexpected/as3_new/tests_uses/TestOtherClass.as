package tests_uses
{
   public class TestOtherClass
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
      
      public function TestOtherClass()
      {
         method
            name "tests_uses:TestOtherClass/TestOtherClass"
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
      
      public function methodBody() : void
      {
         trait method QName(PackageNamespace(""),"methodBody")
            dispid 0
            method
               name "tests_uses:TestOtherClass/methodBody"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 2
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "tc", 0, 13
                     findpropstrict QName(PackageNamespace("tests_uses"),"TestClass")
                     constructprop QName(PackageNamespace("tests_uses"),"TestClass"), 0
                     coerce QName(PackageNamespace("tests_uses"),"TestClass")
                     setlocal1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_uses"),PackageInternalNs("tests_uses"),PrivateNamespace("tests_uses:TestOtherClass"),ProtectedNamespace("tests_uses:TestOtherClass"),StaticProtectedNs("tests_uses:TestOtherClass"),PrivateNamespace("TestOtherClass.as$0")])
                     pushstring "method"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_uses"),PackageInternalNs("tests_uses"),PrivateNamespace("tests_uses:TestOtherClass"),ProtectedNamespace("tests_uses:TestOtherClass"),StaticProtectedNs("tests_uses:TestOtherClass"),PrivateNamespace("TestOtherClass.as$0")]), 1
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
         
         public function argsMethod(tc:TestClass) : void
         {
            trait method QName(PackageNamespace(""),"argsMethod")
               dispid 0
               method
                  name "tests_uses:TestOtherClass/argsMethod"
                  param QName(PackageNamespace("tests_uses"),"TestClass")
                  returns QName(PackageNamespace(""),"void")
                  
                  body
                     maxstack 2
                     localcount 2
                     initscopedepth 4
                     maxscopedepth 5
                     
                     code
                        getlocal0
                        pushscope
                        debug 1, "tc", 0, 0
                        findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_uses"),PackageInternalNs("tests_uses"),PrivateNamespace("tests_uses:TestOtherClass"),ProtectedNamespace("tests_uses:TestOtherClass"),StaticProtectedNs("tests_uses:TestOtherClass"),PrivateNamespace("TestOtherClass.as$0")])
                        pushstring "argsMethod"
                        callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_uses"),PackageInternalNs("tests_uses"),PrivateNamespace("tests_uses:TestOtherClass"),ProtectedNamespace("tests_uses:TestOtherClass"),StaticProtectedNs("tests_uses:TestOtherClass"),PrivateNamespace("TestOtherClass.as$0")]), 1
                        returnvoid
                     end ; code
                  end ; body
               end ; method
            }
            
            public function returnTypeMethod() : TestClass
            {
               trait method QName(PackageNamespace(""),"returnTypeMethod")
                  dispid 0
                  method
                     name "tests_uses:TestOtherClass/returnTypeMethod"
                     returns QName(PackageNamespace("tests_uses"),"TestClass")
                     
                     body
                        maxstack 2
                        localcount 1
                        initscopedepth 4
                        maxscopedepth 5
                        
                        code
                           getlocal0
                           pushscope
                           findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_uses"),PackageInternalNs("tests_uses"),PrivateNamespace("tests_uses:TestOtherClass"),ProtectedNamespace("tests_uses:TestOtherClass"),StaticProtectedNs("tests_uses:TestOtherClass"),PrivateNamespace("TestOtherClass.as$0")])
                           pushstring "returnTypeMethod"
                           callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_uses"),PackageInternalNs("tests_uses"),PrivateNamespace("tests_uses:TestOtherClass"),ProtectedNamespace("tests_uses:TestOtherClass"),StaticProtectedNs("tests_uses:TestOtherClass"),PrivateNamespace("TestOtherClass.as$0")]), 1
                           pushnull
                           returnvalue
                        end ; code
                     end ; body
                  end ; method
               }
               
               public function methodCall() : void
               {
                  trait method QName(PackageNamespace(""),"methodCall")
                     dispid 0
                     method
                        name "tests_uses:TestOtherClass/methodCall"
                        returns QName(PackageNamespace(""),"void")
                        
                        body
                           maxstack 1
                           localcount 2
                           initscopedepth 4
                           maxscopedepth 5
                           
                           code
                              getlocal0
                              pushscope
                              debug 1, "tc", 0, 30
                              findpropstrict QName(PackageNamespace("tests_uses"),"TestClass")
                              constructprop QName(PackageNamespace("tests_uses"),"TestClass"), 0
                              coerce QName(PackageNamespace("tests_uses"),"TestClass")
                              setlocal1
                              getlocal1
                              callpropvoid QName(PackageNamespace(""),"classMethod"), 0
                              returnvoid
                           end ; code
                        end ; body
                     end ; method
                  }
                  
                  public function methodCall2() : void
                  {
                     trait method QName(PackageNamespace(""),"methodCall2")
                        dispid 0
                        method
                           name "tests_uses:TestOtherClass/methodCall2"
                           returns QName(PackageNamespace(""),"void")
                           
                           body
                              maxstack 1
                              localcount 2
                              initscopedepth 4
                              maxscopedepth 5
                              
                              code
                                 getlocal0
                                 pushscope
                                 debug 1, "tc2", 0, 36
                                 findpropstrict QName(PackageNamespace("tests_uses"),"TestClass2")
                                 constructprop QName(PackageNamespace("tests_uses"),"TestClass2"), 0
                                 coerce QName(PackageNamespace("tests_uses"),"TestClass2")
                                 setlocal1
                                 getlocal1
                                 callpropvoid QName(PackageNamespace(""),"classMethod"), 0
                                 returnvoid
                              end ; code
                           end ; body
                        end ; method
                     }
                     
                     public function varUse() : void
                     {
                        trait method QName(PackageNamespace(""),"varUse")
                           dispid 0
                           method
                              name "tests_uses:TestOtherClass/varUse"
                              returns QName(PackageNamespace(""),"void")
                              
                              body
                                 maxstack 2
                                 localcount 2
                                 initscopedepth 4
                                 maxscopedepth 5
                                 
                                 code
                                    getlocal0
                                    pushscope
                                    debug 1, "tc", 0, 42
                                    findpropstrict QName(PackageNamespace("tests_uses"),"TestClass")
                                    constructprop QName(PackageNamespace("tests_uses"),"TestClass"), 0
                                    coerce QName(PackageNamespace("tests_uses"),"TestClass")
                                    setlocal1
                                    findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_uses"),PackageInternalNs("tests_uses"),PrivateNamespace("tests_uses:TestOtherClass"),ProtectedNamespace("tests_uses:TestOtherClass"),StaticProtectedNs("tests_uses:TestOtherClass"),PrivateNamespace("TestOtherClass.as$0")])
                                    getlocal1
                                    getproperty QName(PackageNamespace(""),"parentVar")
                                    callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_uses"),PackageInternalNs("tests_uses"),PrivateNamespace("tests_uses:TestOtherClass"),ProtectedNamespace("tests_uses:TestOtherClass"),StaticProtectedNs("tests_uses:TestOtherClass"),PrivateNamespace("TestOtherClass.as$0")]), 1
                                    findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_uses"),PackageInternalNs("tests_uses"),PrivateNamespace("tests_uses:TestOtherClass"),ProtectedNamespace("tests_uses:TestOtherClass"),StaticProtectedNs("tests_uses:TestOtherClass"),PrivateNamespace("TestOtherClass.as$0")])
                                    getlocal1
                                    getproperty QName(PackageNamespace(""),"classVar")
                                    callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_uses"),PackageInternalNs("tests_uses"),PrivateNamespace("tests_uses:TestOtherClass"),ProtectedNamespace("tests_uses:TestOtherClass"),StaticProtectedNs("tests_uses:TestOtherClass"),PrivateNamespace("TestOtherClass.as$0")]), 1
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
                           findpropstrict Multiname("TestOtherClass",[PackageNamespace("tests_uses")])
                           getlex QName(PackageNamespace(""),"Object")
                           pushscope
                           getlex QName(PackageNamespace(""),"Object")
                           newclass 0
                           popscope
                           initproperty QName(PackageNamespace("tests_uses"),"TestOtherClass")
                           returnvoid
                        end ; code
                     end ; body
                  end ; method
                  
