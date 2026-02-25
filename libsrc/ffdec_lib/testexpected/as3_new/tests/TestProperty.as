package tests
{
   public class TestProperty
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
      
      public function TestProperty()
      {
         method
            name "tests:TestProperty/TestProperty"
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
               name "tests:TestProperty/run"
               returns null
               
               body
                  maxstack 3
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "d", 0, 13
                     debug 1, "k", 1, 14
                     findpropstrict QName(PrivateNamespace("TestProperty.as$0"),"TestClass1")
                     constructprop QName(PrivateNamespace("TestProperty.as$0"),"TestClass1"), 0
                     coerce QName(PrivateNamespace("TestProperty.as$0"),"TestClass1")
                     setlocal1
                     pushbyte 7
                     pushbyte 8
                     add
                     coerce_a
                     setlocal2
                     getlocal2
                     pushbyte 15
                     ifne ofs002c
                     getlocal1
                     getlocal1
                     getproperty Multiname("attrib",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestProperty"),ProtectedNamespace("tests:TestProperty"),StaticProtectedNs("tests:TestProperty"),PrivateNamespace("TestProperty.as$0")])
                     pushbyte 5
                     multiply
                     callpropvoid Multiname("method",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestProperty"),ProtectedNamespace("tests:TestProperty"),StaticProtectedNs("tests:TestProperty"),PrivateNamespace("TestProperty.as$0")]), 1
            ofs002c:
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
      }
   }
   
   class TestClass1
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
      
      public var attrib:int = 5;
      
      public function TestClass1()
      {
         method
            name "TestProperty.as$0:TestClass1/TestClass1"
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
      
      public function method(i:int) : int
      {
         trait method QName(PackageNamespace(""),"method")
            dispid 0
            method
               name "TestProperty.as$0:TestClass1/method"
               param QName(PackageNamespace(""),"int")
               returns QName(PackageNamespace(""),"int")
               
               body
                  maxstack 2
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "i", 0, 0
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PrivateNamespace("TestProperty.as$0"),PrivateNamespace("TestProperty.as$0:TestClass1"),ProtectedNamespace("TestProperty.as$0:TestClass1"),StaticProtectedNs("TestProperty.as$0:TestClass1")])
                     pushstring "method"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PrivateNamespace("TestProperty.as$0"),PrivateNamespace("TestProperty.as$0:TestClass1"),ProtectedNamespace("TestProperty.as$0:TestClass1"),StaticProtectedNs("TestProperty.as$0:TestClass1")]), 1
                     pushbyte 7
                     returnvalue
                  end ; code
               end ; body
            end ; method
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
               findpropstrict Multiname("TestProperty",[PackageNamespace("tests")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 0
               popscope
               initproperty QName(PackageNamespace("tests"),"TestProperty")
               findpropstrict Multiname("TestClass1",[PrivateNamespace("TestProperty.as$0")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 1
               popscope
               initproperty QName(PrivateNamespace("TestProperty.as$0"),"TestClass1")
               returnvoid
            end ; code
         end ; body
      end ; method
      
