package tests
{
   public class TestNames2
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
      
      public var i:int = 0;
      
      public function TestNames2()
      {
         method
            name "tests:TestNames2/TestNames2"
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
               name "tests:TestNames2/run"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 2
                  localcount 3
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "j", 0, 15
                     debug 1, "g", 1, 16
                     pushbyte 0
                     convert_i
                     setlocal1
                     pushnull
                     coerce QName(PackageNamespace(""),"Function")
                     setlocal2
                     getlocal0
                     pushbyte 0
                     setproperty QName(PackageNamespace(""),"i")
                     getlocal0
                     pushbyte 1
                     setproperty QName(PackageNamespace(""),"i")
                     pushbyte 2
                     convert_i
                     setlocal1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestNames2"),ProtectedNamespace("tests:TestNames2"),StaticProtectedNs("tests:TestNames2"),PrivateNamespace("TestNames2.as$0")])
                     getlocal0
                     getproperty QName(PackageNamespace(""),"i")
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestNames2"),ProtectedNamespace("tests:TestNames2"),StaticProtectedNs("tests:TestNames2"),PrivateNamespace("TestNames2.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestNames2"),ProtectedNamespace("tests:TestNames2"),StaticProtectedNs("tests:TestNames2"),PrivateNamespace("TestNames2.as$0")])
                     getlocal0
                     getproperty QName(PackageNamespace(""),"i")
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestNames2"),ProtectedNamespace("tests:TestNames2"),StaticProtectedNs("tests:TestNames2"),PrivateNamespace("TestNames2.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestNames2"),ProtectedNamespace("tests:TestNames2"),StaticProtectedNs("tests:TestNames2"),PrivateNamespace("TestNames2.as$0")])
                     getlocal1
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestNames2"),ProtectedNamespace("tests:TestNames2"),StaticProtectedNs("tests:TestNames2"),PrivateNamespace("TestNames2.as$0")]), 1
                     getlocal0
                     callpropvoid QName(PackageNamespace(""),"f"), 0
                     getlocal0
                     callpropvoid QName(PackageNamespace(""),"f"), 0
                     getlocal2
                     getglobalscope
                     call 0
                     pop
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
         
         public function f() : void
         {
            trait method QName(PackageNamespace(""),"f")
               dispid 0
               method
                  name "tests:TestNames2/f"
                  returns QName(PackageNamespace(""),"void")
                  
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
               findpropstrict Multiname("TestNames2",[PackageNamespace("tests")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 0
               popscope
               initproperty QName(PackageNamespace("tests"),"TestNames2")
               returnvoid
            end ; code
         end ; body
      end ; method
      
