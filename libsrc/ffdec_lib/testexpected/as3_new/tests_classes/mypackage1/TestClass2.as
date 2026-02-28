package tests_classes.mypackage1
{
   import tests_classes.mypackage2.TestClass;
   import tests_classes.mypackage3.TestClass;
   
   use namespace myNamespace;
   
   public class TestClass2
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
      
      public function TestClass2()
      {
         method
            name "tests_classes.mypackage1:TestClass2/TestClass2"
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
      
      myNamespace static function testCall5() : String
      {
         trait method QName(Namespace("https://www.free-decompiler.com/flash/test/namespace"),"testCall5")
            flag FINAL
            dispid 3
            method
               name "tests_classes.mypackage1:TestClass2myNamespace/testCall5"
               returns QName(PackageNamespace(""),"String")
               
               body
                  maxstack 1
                  localcount 1
                  initscopedepth 3
                  maxscopedepth 4
                  
                  code
                     getlocal0
                     pushscope
                     pushstring "x"
                     returnvalue
                  end ; code
               end ; body
            end ; method
         }
         
         protected static function testCall5() : String
         {
            trait method QName(StaticProtectedNs("tests_classes.mypackage1:TestClass2"),"testCall5")
               flag FINAL
               dispid 4
               method
                  name "tests_classes.mypackage1:TestClass2/protected/testCall5"
                  returns QName(PackageNamespace(""),"String")
                  
                  body
                     maxstack 1
                     localcount 1
                     initscopedepth 3
                     maxscopedepth 4
                     
                     code
                        getlocal0
                        pushscope
                        pushstring "5"
                        returnvalue
                     end ; code
                  end ; body
               end ; method
            }
            
            public function testCall() : String
            {
               trait method QName(PackageNamespace(""),"testCall")
                  dispid 0
                  method
                     name "tests_classes.mypackage1:TestClass2/testCall"
                     returns QName(PackageNamespace(""),"String")
                     
                     body
                        maxstack 3
                        localcount 6
                        initscopedepth 4
                        maxscopedepth 5
                        
                        code
                           getlocal0
                           pushscope
                           debug 1, "a", 0, 28
                           debug 1, "b", 1, 29
                           debug 1, "c", 2, 30
                           debug 1, "res", 3, 34
                           pushnull
                           coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestClass")
                           setlocal1
                           pushnull
                           coerce QName(PackageNamespace("tests_classes.mypackage2"),"TestClass")
                           setlocal2
                           pushnull
                           coerce QName(PackageNamespace("tests_classes.mypackage3"),"TestClass")
                           setlocal3
                           findpropstrict QName(PackageNamespace("tests_classes.mypackage1"),"TestClass")
                           constructprop QName(PackageNamespace("tests_classes.mypackage1"),"TestClass"), 0
                           coerce QName(PackageNamespace("tests_classes.mypackage1"),"TestClass")
                           setlocal1
                           findpropstrict QName(PackageNamespace("tests_classes.mypackage2"),"TestClass")
                           constructprop QName(PackageNamespace("tests_classes.mypackage2"),"TestClass"), 0
                           coerce QName(PackageNamespace("tests_classes.mypackage2"),"TestClass")
                           setlocal2
                           findpropstrict QName(PackageNamespace("tests_classes.mypackage3"),"TestClass")
                           constructprop QName(PackageNamespace("tests_classes.mypackage3"),"TestClass"), 0
                           coerce QName(PackageNamespace("tests_classes.mypackage3"),"TestClass")
                           setlocal3
                           getlocal1
                           callproperty QName(PackageNamespace(""),"testCall"), 0
                           getlocal2
                           callproperty QName(PackageNamespace(""),"testCall"), 0
                           add
                           getlocal3
                           callproperty QName(PackageNamespace(""),"testCall"), 0
                           add
                           getlocal0
                           callproperty QName(PackageNamespace(""),"testCall2"), 0
                           add
                           getlocal0
                           callproperty QName(PrivateNamespace("tests_classes.mypackage1:TestClass2"),"testCall3"), 0
                           add
                           getlocal0
                           callproperty QName(ProtectedNamespace("tests_classes.mypackage1:TestClass2"),"testCall4"), 0
                           add
                           findpropstrict QName(StaticProtectedNs("tests_classes.mypackage1:TestClass2"),"testCall5")
                           callproperty QName(StaticProtectedNs("tests_classes.mypackage1:TestClass2"),"testCall5"), 0
                           add
                           getlocal0
                           callproperty QName(PackageInternalNs("tests_classes.mypackage1"),"testCall6"), 0
                           add
                           getlex QName(PackageNamespace("tests_classes.mypackage1"),"myNamespace")
                           coerce QName(PackageNamespace(""),"Namespace")
                           findpropstrict RTQName("testCall3")
                           dup
                           setlocal 5
                           getlex QName(PackageNamespace("tests_classes.mypackage1"),"myNamespace")
                           coerce QName(PackageNamespace(""),"Namespace")
                           getproperty RTQName("testCall3")
                           getlocal 5
                           call 0
                           kill 5
                           add
                           coerce_s
                           setlocal 4
                           findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes.mypackage1"),PackageInternalNs("tests_classes.mypackage1"),Namespace("https://www.free-decompiler.com/flash/test/namespace"),PrivateNamespace("tests_classes.mypackage1:TestClass2"),ProtectedNamespace("tests_classes.mypackage1:TestClass2"),StaticProtectedNs("tests_classes.mypackage1:TestClass2"),PrivateNamespace("TestClass2.as$0")])
                           getlocal 4
                           callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes.mypackage1"),PackageInternalNs("tests_classes.mypackage1"),Namespace("https://www.free-decompiler.com/flash/test/namespace"),PrivateNamespace("tests_classes.mypackage1:TestClass2"),ProtectedNamespace("tests_classes.mypackage1:TestClass2"),StaticProtectedNs("tests_classes.mypackage1:TestClass2"),PrivateNamespace("TestClass2.as$0")]), 1
                           getlocal 4
                           returnvalue
                        end ; code
                     end ; body
                  end ; method
               }
               
               myNamespace function testCall2() : String
               {
                  trait method QName(Namespace("https://www.free-decompiler.com/flash/test/namespace"),"testCall2")
                     dispid 0
                     method
                        name "tests_classes.mypackage1:TestClass2myNamespace/testCall2"
                        returns QName(PackageNamespace(""),"String")
                        
                        body
                           maxstack 1
                           localcount 1
                           initscopedepth 4
                           maxscopedepth 5
                           
                           code
                              getlocal0
                              pushscope
                              pushstring "1"
                              returnvalue
                           end ; code
                        end ; body
                     end ; method
                  }
                  
                  myNamespace function testCall3() : String
                  {
                     trait method QName(Namespace("https://www.free-decompiler.com/flash/test/namespace"),"testCall3")
                        dispid 0
                        method
                           name "tests_classes.mypackage1:TestClass2myNamespace/testCall3"
                           returns QName(PackageNamespace(""),"String")
                           
                           body
                              maxstack 2
                              localcount 2
                              initscopedepth 4
                              maxscopedepth 5
                              
                              code
                                 getlocal0
                                 pushscope
                                 getlex QName(PackageNamespace("tests_classes.mypackage1"),"myNamespace")
                                 coerce QName(PackageNamespace(""),"Namespace")
                                 findpropstrict RTQName("testCall2")
                                 dup
                                 setlocal1
                                 getlex QName(PackageNamespace("tests_classes.mypackage1"),"myNamespace")
                                 coerce QName(PackageNamespace(""),"Namespace")
                                 getproperty RTQName("testCall2")
                                 getlocal1
                                 call 0
                                 kill 1
                                 returnvalue
                              end ; code
                           end ; body
                        end ; method
                     }
                     
                     myNamespace function testCall4() : String
                     {
                        trait method QName(Namespace("https://www.free-decompiler.com/flash/test/namespace"),"testCall4")
                           dispid 0
                           method
                              name "tests_classes.mypackage1:TestClass2myNamespace/testCall4"
                              returns QName(PackageNamespace(""),"String")
                              
                              body
                                 maxstack 2
                                 localcount 2
                                 initscopedepth 4
                                 maxscopedepth 5
                                 
                                 code
                                    getlocal0
                                    pushscope
                                    getlex QName(PackageNamespace("tests_classes.mypackage1"),"myNamespace")
                                    coerce QName(PackageNamespace(""),"Namespace")
                                    findpropstrict RTQName("testCall3")
                                    dup
                                    setlocal1
                                    getlex QName(PackageNamespace("tests_classes.mypackage1"),"myNamespace")
                                    coerce QName(PackageNamespace(""),"Namespace")
                                    getproperty RTQName("testCall3")
                                    getlocal1
                                    call 0
                                    kill 1
                                    returnvalue
                                 end ; code
                              end ; body
                           end ; method
                        }
                        
                        myNamespace function testCall6() : String
                        {
                           trait method QName(Namespace("https://www.free-decompiler.com/flash/test/namespace"),"testCall6")
                              dispid 0
                              method
                                 name "tests_classes.mypackage1:TestClass2myNamespace/testCall6"
                                 returns QName(PackageNamespace(""),"String")
                                 
                                 body
                                    maxstack 1
                                    localcount 1
                                    initscopedepth 4
                                    maxscopedepth 5
                                    
                                    code
                                       getlocal0
                                       pushscope
                                       pushstring "y"
                                       returnvalue
                                    end ; code
                                 end ; body
                              end ; method
                           }
                           
                           public function testCall2() : String
                           {
                              trait method QName(PackageNamespace(""),"testCall2")
                                 dispid 0
                                 method
                                    name "tests_classes.mypackage1:TestClass2/testCall2"
                                    returns QName(PackageNamespace(""),"String")
                                    
                                    body
                                       maxstack 1
                                       localcount 1
                                       initscopedepth 4
                                       maxscopedepth 5
                                       
                                       code
                                          getlocal0
                                          pushscope
                                          pushstring "2"
                                          returnvalue
                                       end ; code
                                    end ; body
                                 end ; method
                              }
                              
                              private function testCall3() : String
                              {
                                 trait method QName(PrivateNamespace("tests_classes.mypackage1:TestClass2"),"testCall3")
                                    dispid 0
                                    method
                                       name "tests_classes.mypackage1:TestClass2/private/testCall3"
                                       returns QName(PackageNamespace(""),"String")
                                       
                                       body
                                          maxstack 1
                                          localcount 1
                                          initscopedepth 4
                                          maxscopedepth 5
                                          
                                          code
                                             getlocal0
                                             pushscope
                                             pushstring "3"
                                             returnvalue
                                          end ; code
                                       end ; body
                                    end ; method
                                 }
                                 
                                 protected function testCall4() : String
                                 {
                                    trait method QName(ProtectedNamespace("tests_classes.mypackage1:TestClass2"),"testCall4")
                                       dispid 0
                                       method
                                          name "tests_classes.mypackage1:TestClass2/protected/testCall4"
                                          returns QName(PackageNamespace(""),"String")
                                          
                                          body
                                             maxstack 1
                                             localcount 1
                                             initscopedepth 4
                                             maxscopedepth 5
                                             
                                             code
                                                getlocal0
                                                pushscope
                                                pushstring "4"
                                                returnvalue
                                             end ; code
                                          end ; body
                                       end ; method
                                    }
                                    
                                    internal function testCall6() : String
                                    {
                                       trait method QName(PackageInternalNs("tests_classes.mypackage1"),"testCall6")
                                          dispid 0
                                          method
                                             name "tests_classes.mypackage1:TestClass2tests_classes.mypackage1/testCall6"
                                             returns QName(PackageNamespace(""),"String")
                                             
                                             body
                                                maxstack 1
                                                localcount 1
                                                initscopedepth 4
                                                maxscopedepth 5
                                                
                                                code
                                                   getlocal0
                                                   pushscope
                                                   pushstring "6"
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
                                          findpropstrict Multiname("TestClass2",[PackageNamespace("tests_classes.mypackage1")])
                                          getlex QName(PackageNamespace(""),"Object")
                                          pushscope
                                          getlex QName(PackageNamespace(""),"Object")
                                          newclass 0
                                          popscope
                                          initproperty QName(PackageNamespace("tests_classes.mypackage1"),"TestClass2")
                                          returnvoid
                                       end ; code
                                    end ; body
                                 end ; method
                                 
