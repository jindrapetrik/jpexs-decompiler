package tests_uses
{
   public class TestClass extends TestParentClass implements TestInterface
   {
      
      method
         name ""
         returns null
         
         body
            maxstack 1
            localcount 1
            initscopedepth 5
            maxscopedepth 6
            
            code
               getlocal0
               pushscope
               returnvoid
            end ; code
         end ; body
      end ; method
      
      public var classVar:int = 2;
      
      public function TestClass()
      {
         method
            name "tests_uses:TestClass/TestClass"
            returns null
            
            body
               maxstack 1
               localcount 1
               initscopedepth 5
               maxscopedepth 6
               
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
      
      public function interfaceMethod() : void
      {
         trait method QName(PackageNamespace(""),"interfaceMethod")
            dispid 0
            method
               name "tests_uses:TestClass/interfaceMethod"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 2
                  localcount 1
                  initscopedepth 5
                  maxscopedepth 6
                  
                  code
                     getlocal0
                     pushscope
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_uses"),PackageInternalNs("tests_uses"),StaticProtectedNs("tests_uses:TestParentClass"),PrivateNamespace("tests_uses:TestClass"),ProtectedNamespace("tests_uses:TestClass"),StaticProtectedNs("tests_uses:TestClass"),PrivateNamespace("TestClass.as$0")])
                     pushstring "interfaceMethod"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_uses"),PackageInternalNs("tests_uses"),StaticProtectedNs("tests_uses:TestParentClass"),PrivateNamespace("tests_uses:TestClass"),ProtectedNamespace("tests_uses:TestClass"),StaticProtectedNs("tests_uses:TestClass"),PrivateNamespace("TestClass.as$0")]), 1
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
         
         public function parentInterfaceMethod() : void
         {
            trait method QName(PackageNamespace(""),"parentInterfaceMethod")
               dispid 0
               method
                  name "tests_uses:TestClass/parentInterfaceMethod"
                  returns QName(PackageNamespace(""),"void")
                  
                  body
                     maxstack 2
                     localcount 1
                     initscopedepth 5
                     maxscopedepth 6
                     
                     code
                        getlocal0
                        pushscope
                        findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_uses"),PackageInternalNs("tests_uses"),StaticProtectedNs("tests_uses:TestParentClass"),PrivateNamespace("tests_uses:TestClass"),ProtectedNamespace("tests_uses:TestClass"),StaticProtectedNs("tests_uses:TestClass"),PrivateNamespace("TestClass.as$0")])
                        pushstring "parentInterfaceMethod"
                        callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_uses"),PackageInternalNs("tests_uses"),StaticProtectedNs("tests_uses:TestParentClass"),PrivateNamespace("tests_uses:TestClass"),ProtectedNamespace("tests_uses:TestClass"),StaticProtectedNs("tests_uses:TestClass"),PrivateNamespace("TestClass.as$0")]), 1
                        returnvoid
                     end ; code
                  end ; body
               end ; method
            }
            
            public function classMethod() : void
            {
               trait method QName(PackageNamespace(""),"classMethod")
                  dispid 0
                  method
                     name "tests_uses:TestClass/classMethod"
                     returns QName(PackageNamespace(""),"void")
                     
                     body
                        maxstack 2
                        localcount 1
                        initscopedepth 5
                        maxscopedepth 6
                        
                        code
                           getlocal0
                           pushscope
                           findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_uses"),PackageInternalNs("tests_uses"),StaticProtectedNs("tests_uses:TestParentClass"),PrivateNamespace("tests_uses:TestClass"),ProtectedNamespace("tests_uses:TestClass"),StaticProtectedNs("tests_uses:TestClass"),PrivateNamespace("TestClass.as$0")])
                           pushstring "classMethod"
                           callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_uses"),PackageInternalNs("tests_uses"),StaticProtectedNs("tests_uses:TestParentClass"),PrivateNamespace("tests_uses:TestClass"),ProtectedNamespace("tests_uses:TestClass"),StaticProtectedNs("tests_uses:TestClass"),PrivateNamespace("TestClass.as$0")]), 1
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
               maxscopedepth 4
               
               code
                  getlocal0
                  pushscope
                  findpropstrict Multiname("TestClass",[PackageNamespace("tests_uses")])
                  getlex QName(PackageNamespace(""),"Object")
                  pushscope
                  getlex QName(PackageNamespace("tests_uses"),"TestParentClass")
                  pushscope
                  getlex QName(PackageNamespace("tests_uses"),"TestParentClass")
                  newclass 0
                  popscope
                  popscope
                  initproperty QName(PackageNamespace("tests_uses"),"TestClass")
                  returnvoid
               end ; code
            end ; body
         end ; method
         
