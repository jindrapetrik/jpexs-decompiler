package tests
{
   public class TestStrictEquals
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
      
      public function TestStrictEquals()
      {
         method
            name "tests:TestStrictEquals/TestStrictEquals"
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
               name "tests:TestStrictEquals/run"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 2
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "k", 0, 13
                     pushbyte 6
                     convert_i
                     setlocal1
                     getlocal0
                     callproperty QName(PrivateNamespace("tests:TestStrictEquals"),"f"), 0
                     getlocal0
                     callproperty QName(PrivateNamespace("tests:TestStrictEquals"),"f"), 0
                     ifstricteq ofs001e
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestStrictEquals"),ProtectedNamespace("tests:TestStrictEquals"),StaticProtectedNs("tests:TestStrictEquals"),PrivateNamespace("TestStrictEquals.as$0")])
                     pushstring "is eight"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestStrictEquals"),ProtectedNamespace("tests:TestStrictEquals"),StaticProtectedNs("tests:TestStrictEquals"),PrivateNamespace("TestStrictEquals.as$0")]), 1
            ofs001e:
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
         
         private function f() : String
         {
            trait method QName(PrivateNamespace("tests:TestStrictEquals"),"f")
               dispid 0
               method
                  name "tests:TestStrictEquals/private/f"
                  returns QName(PackageNamespace(""),"String")
                  
                  body
                     maxstack 1
                     localcount 1
                     initscopedepth 4
                     maxscopedepth 5
                     
                     code
                        getlocal0
                        pushscope
                        pushstring "x"
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
               findpropstrict Multiname("TestStrictEquals",[PackageNamespace("tests")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 0
               popscope
               initproperty QName(PackageNamespace("tests"),"TestStrictEquals")
               returnvoid
            end ; code
         end ; body
      end ; method
      
