package tests
{
   public class TestStringConcat
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
      
      public function TestStringConcat()
      {
         method
            name "tests:TestStringConcat/TestStringConcat"
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
               name "tests:TestStringConcat/run"
               returns null
               
               body
                  maxstack 4
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "k", 0, 13
                     pushbyte 8
                     convert_i
                     setlocal1
                     getlocal0
                     pushstring "hello"
                     pushbyte 5
                     pushbyte 6
                     multiply
                     add
                     callpropvoid QName(PrivateNamespace("tests:TestStringConcat"),"traceIt"), 1
                     getlocal0
                     pushstring "hello"
                     getlocal1
                     pushbyte 1
                     subtract
                     add
                     callpropvoid QName(PrivateNamespace("tests:TestStringConcat"),"traceIt"), 1
                     getlocal0
                     pushstring "hello"
                     pushbyte 5
                     add
                     pushbyte 6
                     add
                     callpropvoid QName(PrivateNamespace("tests:TestStringConcat"),"traceIt"), 1
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
         
         private function traceIt(s:String) : void
         {
            trait method QName(PrivateNamespace("tests:TestStringConcat"),"traceIt")
               dispid 0
               method
                  name "tests:TestStringConcat/private/traceIt"
                  param QName(PackageNamespace(""),"String")
                  returns QName(PackageNamespace(""),"void")
                  
                  body
                     maxstack 2
                     localcount 2
                     initscopedepth 4
                     maxscopedepth 5
                     
                     code
                        getlocal0
                        pushscope
                        debug 1, "s", 0, 0
                        findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestStringConcat"),ProtectedNamespace("tests:TestStringConcat"),StaticProtectedNs("tests:TestStringConcat"),PrivateNamespace("TestStringConcat.as$0")])
                        getlocal1
                        callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestStringConcat"),ProtectedNamespace("tests:TestStringConcat"),StaticProtectedNs("tests:TestStringConcat"),PrivateNamespace("TestStringConcat.as$0")]), 1
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
               findpropstrict Multiname("TestStringConcat",[PackageNamespace("tests")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 0
               popscope
               initproperty QName(PackageNamespace("tests"),"TestStringConcat")
               returnvoid
            end ; code
         end ; body
      end ; method
      
