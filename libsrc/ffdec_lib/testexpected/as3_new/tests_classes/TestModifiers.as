package tests_classes
{
   import tests_other.myInternal;
   import tests_other.myInternal2;
   
   use namespace myInternal;
   use namespace myInternal2;
   
   public class TestModifiers
   {
      
      private static var attr_stat_private:int = 1;
      
      public static var attr_stat_public:int = 2;
      
      internal static var attr_stat_internal:int = 3;
      
      protected static var attr_stat_protected:int = 4;
      
      myInternal static var attr_stat_namespace_explicit:int = 5;
      
      myInternal2 static var attr_stat_namespace_implicit:int = 6;
      
      method
         name ""
         returns null
         
         body
            maxstack 2
            localcount 1
            initscopedepth 3
            maxscopedepth 4
            
            code
               getlocal0
               pushscope
               findproperty QName(PrivateNamespace("tests_classes:TestModifiers"),"attr_stat_private")
               pushbyte 1
               setproperty QName(PrivateNamespace("tests_classes:TestModifiers"),"attr_stat_private")
               findproperty QName(PackageNamespace(""),"attr_stat_public")
               pushbyte 2
               setproperty QName(PackageNamespace(""),"attr_stat_public")
               findproperty QName(PackageInternalNs("tests_classes"),"attr_stat_internal")
               pushbyte 3
               setproperty QName(PackageInternalNs("tests_classes"),"attr_stat_internal")
               findproperty QName(StaticProtectedNs("tests_classes:TestModifiers"),"attr_stat_protected")
               pushbyte 4
               setproperty QName(StaticProtectedNs("tests_classes:TestModifiers"),"attr_stat_protected")
               findproperty QName(Namespace("http://www.adobe.com/2006/actionscript/examples"),"attr_stat_namespace_explicit")
               pushbyte 5
               setproperty QName(Namespace("http://www.adobe.com/2006/actionscript/examples"),"attr_stat_namespace_explicit")
               findproperty QName(PackageInternalNs("tests_other:myInternal2"),"attr_stat_namespace_implicit")
               pushbyte 6
               setproperty QName(PackageInternalNs("tests_other:myInternal2"),"attr_stat_namespace_implicit")
               returnvoid
            end ; code
         end ; body
      end ; method
      
      private var attr_inst_private:int = 7;
      
      public var attr_inst_public:int = 8;
      
      internal var attr_inst_internal:int = 9;
      
      protected var attr_inst_protected:int = 10;
      
      myInternal var attr_inst_namespace_explicit:int = 11;
      
      myInternal2 var attr_inst_namespace_implicit:int = 12;
      
      public function TestModifiers()
      {
         method
            name "tests_classes:TestModifiers/TestModifiers"
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
      
      private static function func_stat_private() : int
      {
         trait method QName(PrivateNamespace("tests_classes:TestModifiers"),"func_stat_private")
            flag FINAL
            dispid 3
            method
               name "tests_classes:TestModifiers/private/func_stat_private"
               returns QName(PackageNamespace(""),"int")
               
               body
                  maxstack 1
                  localcount 1
                  initscopedepth 3
                  maxscopedepth 4
                  
                  code
                     getlocal0
                     pushscope
                     pushbyte 1
                     returnvalue
                  end ; code
               end ; body
            end ; method
         }
         
         public static function func_stat_public() : int
         {
            trait method QName(PackageNamespace(""),"func_stat_public")
               flag FINAL
               dispid 4
               method
                  name "tests_classes:TestModifiers/func_stat_public"
                  returns QName(PackageNamespace(""),"int")
                  
                  body
                     maxstack 1
                     localcount 1
                     initscopedepth 3
                     maxscopedepth 4
                     
                     code
                        getlocal0
                        pushscope
                        pushbyte 2
                        returnvalue
                     end ; code
                  end ; body
               end ; method
            }
            
            internal static function func_stat_internal() : int
            {
               trait method QName(PackageInternalNs("tests_classes"),"func_stat_internal")
                  flag FINAL
                  dispid 5
                  method
                     name "tests_classes:TestModifierstests_classes/func_stat_internal"
                     returns QName(PackageNamespace(""),"int")
                     
                     body
                        maxstack 1
                        localcount 1
                        initscopedepth 3
                        maxscopedepth 4
                        
                        code
                           getlocal0
                           pushscope
                           pushbyte 3
                           returnvalue
                        end ; code
                     end ; body
                  end ; method
               }
               
               protected static function func_stat_protected() : int
               {
                  trait method QName(StaticProtectedNs("tests_classes:TestModifiers"),"func_stat_protected")
                     flag FINAL
                     dispid 6
                     method
                        name "tests_classes:TestModifiers/protected/func_stat_protected"
                        returns QName(PackageNamespace(""),"int")
                        
                        body
                           maxstack 1
                           localcount 1
                           initscopedepth 3
                           maxscopedepth 4
                           
                           code
                              getlocal0
                              pushscope
                              pushbyte 4
                              returnvalue
                           end ; code
                        end ; body
                     end ; method
                  }
                  
                  myInternal static function func_stat_namespace_explicit() : int
                  {
                     trait method QName(Namespace("http://www.adobe.com/2006/actionscript/examples"),"func_stat_namespace_explicit")
                        flag FINAL
                        dispid 7
                        method
                           name "tests_classes:TestModifiersmyInternal/func_stat_namespace_explicit"
                           returns QName(PackageNamespace(""),"int")
                           
                           body
                              maxstack 1
                              localcount 1
                              initscopedepth 3
                              maxscopedepth 4
                              
                              code
                                 getlocal0
                                 pushscope
                                 pushbyte 5
                                 returnvalue
                              end ; code
                           end ; body
                        end ; method
                     }
                     
                     myInternal2 static function func_stat_namespace_implicit() : int
                     {
                        trait method QName(PackageInternalNs("tests_other:myInternal2"),"func_stat_namespace_implicit")
                           flag FINAL
                           dispid 8
                           method
                              name "tests_classes:TestModifiersmyInternal2/func_stat_namespace_implicit"
                              returns QName(PackageNamespace(""),"int")
                              
                              body
                                 maxstack 1
                                 localcount 1
                                 initscopedepth 3
                                 maxscopedepth 4
                                 
                                 code
                                    getlocal0
                                    pushscope
                                    pushbyte 6
                                    returnvalue
                                 end ; code
                              end ; body
                           end ; method
                        }
                        
                        private function func_inst_private() : int
                        {
                           trait method QName(PrivateNamespace("tests_classes:TestModifiers"),"func_inst_private")
                              dispid 0
                              method
                                 name "tests_classes:TestModifiers/private/func_inst_private"
                                 returns QName(PackageNamespace(""),"int")
                                 
                                 body
                                    maxstack 1
                                    localcount 1
                                    initscopedepth 4
                                    maxscopedepth 5
                                    
                                    code
                                       getlocal0
                                       pushscope
                                       pushbyte 7
                                       returnvalue
                                    end ; code
                                 end ; body
                              end ; method
                           }
                           
                           public function func_inst_public() : int
                           {
                              trait method QName(PackageNamespace(""),"func_inst_public")
                                 dispid 0
                                 method
                                    name "tests_classes:TestModifiers/func_inst_public"
                                    returns QName(PackageNamespace(""),"int")
                                    
                                    body
                                       maxstack 1
                                       localcount 1
                                       initscopedepth 4
                                       maxscopedepth 5
                                       
                                       code
                                          getlocal0
                                          pushscope
                                          pushbyte 8
                                          returnvalue
                                       end ; code
                                    end ; body
                                 end ; method
                              }
                              
                              internal function func_inst_internal() : int
                              {
                                 trait method QName(PackageInternalNs("tests_classes"),"func_inst_internal")
                                    dispid 0
                                    method
                                       name "tests_classes:TestModifierstests_classes/func_inst_internal"
                                       returns QName(PackageNamespace(""),"int")
                                       
                                       body
                                          maxstack 1
                                          localcount 1
                                          initscopedepth 4
                                          maxscopedepth 5
                                          
                                          code
                                             getlocal0
                                             pushscope
                                             pushbyte 9
                                             returnvalue
                                          end ; code
                                       end ; body
                                    end ; method
                                 }
                                 
                                 protected function func_inst_protected() : int
                                 {
                                    trait method QName(ProtectedNamespace("tests_classes:TestModifiers"),"func_inst_protected")
                                       dispid 0
                                       method
                                          name "tests_classes:TestModifiers/protected/func_inst_protected"
                                          returns QName(PackageNamespace(""),"int")
                                          
                                          body
                                             maxstack 1
                                             localcount 1
                                             initscopedepth 4
                                             maxscopedepth 5
                                             
                                             code
                                                getlocal0
                                                pushscope
                                                pushbyte 10
                                                returnvalue
                                             end ; code
                                          end ; body
                                       end ; method
                                    }
                                    
                                    myInternal function func_inst_namespace_explicit() : int
                                    {
                                       trait method QName(Namespace("http://www.adobe.com/2006/actionscript/examples"),"func_inst_namespace_explicit")
                                          dispid 0
                                          method
                                             name "tests_classes:TestModifiersmyInternal/func_inst_namespace_explicit"
                                             returns QName(PackageNamespace(""),"int")
                                             
                                             body
                                                maxstack 1
                                                localcount 1
                                                initscopedepth 4
                                                maxscopedepth 5
                                                
                                                code
                                                   getlocal0
                                                   pushscope
                                                   pushbyte 11
                                                   returnvalue
                                                end ; code
                                             end ; body
                                          end ; method
                                       }
                                       
                                       myInternal2 function func_inst_namespace_implicit() : int
                                       {
                                          trait method QName(PackageInternalNs("tests_other:myInternal2"),"func_inst_namespace_implicit")
                                             dispid 0
                                             method
                                                name "tests_classes:TestModifiersmyInternal2/func_inst_namespace_implicit"
                                                returns QName(PackageNamespace(""),"int")
                                                
                                                body
                                                   maxstack 1
                                                   localcount 1
                                                   initscopedepth 4
                                                   maxscopedepth 5
                                                   
                                                   code
                                                      getlocal0
                                                      pushscope
                                                      pushbyte 12
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
                                             findpropstrict Multiname("TestModifiers",[PackageNamespace("tests_classes")])
                                             getlex QName(PackageNamespace(""),"Object")
                                             pushscope
                                             getlex QName(PackageNamespace(""),"Object")
                                             newclass 0
                                             popscope
                                             initproperty QName(PackageNamespace("tests_classes"),"TestModifiers")
                                             returnvoid
                                          end ; code
                                       end ; body
                                    end ; method
                                    
