package tests_classes
{
   public class TestThisOutsideClass
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
      
      public var attrib:int = 0;
      
      public function TestThisOutsideClass()
      {
         method
            name "tests_classes:TestThisOutsideClass/TestThisOutsideClass"
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
      
      public function run() : void
      {
         trait method QName(PackageNamespace(""),"run")
            dispid 0
            method
               name "tests_classes:TestThisOutsideClass/run"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 3
                  localcount 1
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     getlex QName(PrivateNamespace("TestThisOutsideClass.as$0"),"helperFunc")
                     getlocal0
                     pushstring "hello"
                     callpropvoid Multiname("call",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes"),PackageInternalNs("tests_classes"),PrivateNamespace("tests_classes:TestThisOutsideClass"),ProtectedNamespace("tests_classes:TestThisOutsideClass"),StaticProtectedNs("tests_classes:TestThisOutsideClass"),PrivateNamespace("TestThisOutsideClass.as$0")]), 2
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
      }
   }
   
   function helperFunc(a:String) : void
   {
      trait method QName(PrivateNamespace("TestThisOutsideClass.as$0"),"helperFunc")
         dispid 3
         method
            name "/helperFunc"
            param QName(PackageNamespace(""),"String")
            returns QName(PackageNamespace(""),"void")
            
            body
               maxstack 2
               localcount 4
               initscopedepth 1
               maxscopedepth 2
               
               code
                  getlocal0
                  pushscope
                  debug 1, "a", 0, 0
                  findpropstrict QName(PackageNamespace(""),"trace")
                  getlocal1
                  callpropvoid QName(PackageNamespace(""),"trace"), 1
                  getlocal0
                  dup
                  setlocal2
                  getproperty Multiname("attrib",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes"),PackageInternalNs("tests_classes"),PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PrivateNamespace("TestThisOutsideClass.as$0")])
                  convert_d
                  increment
                  setlocal3
                  getlocal2
                  getlocal3
                  setproperty Multiname("attrib",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes"),PackageInternalNs("tests_classes"),PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PrivateNamespace("TestThisOutsideClass.as$0")])
                  kill 3
                  kill 2
                  returnvoid
               end ; code
            end ; body
         end ; method
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
               findpropstrict Multiname("TestThisOutsideClass",[PackageNamespace("tests_classes")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 0
               popscope
               initproperty QName(PackageNamespace("tests_classes"),"TestThisOutsideClass")
               returnvoid
            end ; code
         end ; body
      end ; method
      
