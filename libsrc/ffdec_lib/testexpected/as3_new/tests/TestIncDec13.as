package tests
{
   public class TestIncDec13
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
      
      public function TestIncDec13()
      {
         method
            name "tests:TestIncDec13/TestIncDec13"
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
               name "tests:TestIncDec13/run"
               returns null
               
               body
                  maxstack 5
                  localcount 5
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "a", 0, 13
                     pushbyte 1
                     pushbyte 2
                     pushbyte 3
                     pushbyte 4
                     pushbyte 5
                     newarray 5
                     coerce_a
                     setlocal1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")])
                     pushstring "++a[this.f()] with result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")])
                     getlocal1
                     dup
                     setlocal2
                     getlocal0
                     callproperty QName(PrivateNamespace("tests:TestIncDec13"),"f"), 0
                     dup
                     setlocal3
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")])
                     increment
                     dup
                     setlocal 4
                     getlocal2
                     getlocal3
                     getlocal 4
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")])
                     kill 4
                     kill 2
                     kill 3
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")])
                     pushstring "--a[this.f()] with result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")])
                     getlocal1
                     dup
                     setlocal2
                     getlocal0
                     callproperty QName(PrivateNamespace("tests:TestIncDec13"),"f"), 0
                     dup
                     setlocal3
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")])
                     decrement
                     dup
                     setlocal 4
                     getlocal2
                     getlocal3
                     getlocal 4
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")])
                     kill 4
                     kill 2
                     kill 3
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")])
                     pushstring "++a[this.f()] no result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")]), 1
                     getlocal1
                     dup
                     setlocal2
                     getlocal0
                     callproperty QName(PrivateNamespace("tests:TestIncDec13"),"f"), 0
                     dup
                     setlocal3
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")])
                     increment
                     setlocal 4
                     getlocal2
                     getlocal3
                     getlocal 4
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")])
                     kill 4
                     kill 2
                     kill 3
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")])
                     pushstring "--a[this.f()] no result"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")]), 1
                     getlocal1
                     dup
                     setlocal2
                     getlocal0
                     callproperty QName(PrivateNamespace("tests:TestIncDec13"),"f"), 0
                     dup
                     setlocal3
                     getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")])
                     decrement
                     setlocal 4
                     getlocal2
                     getlocal3
                     getlocal 4
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestIncDec13"),ProtectedNamespace("tests:TestIncDec13"),StaticProtectedNs("tests:TestIncDec13"),PrivateNamespace("TestIncDec13.as$0")])
                     kill 4
                     kill 2
                     kill 3
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
         
         private function f() : int
         {
            trait method QName(PrivateNamespace("tests:TestIncDec13"),"f")
               dispid 0
               method
                  name "tests:TestIncDec13/private/f"
                  returns QName(PackageNamespace(""),"int")
                  
                  body
                     maxstack 1
                     localcount 1
                     initscopedepth 4
                     maxscopedepth 5
                     
                     code
                        getlocal0
                        pushscope
                        pushbyte 0
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
               findpropstrict Multiname("TestIncDec13",[PackageNamespace("tests")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 0
               popscope
               initproperty QName(PackageNamespace("tests"),"TestIncDec13")
               returnvoid
            end ; code
         end ; body
      end ; method
      
