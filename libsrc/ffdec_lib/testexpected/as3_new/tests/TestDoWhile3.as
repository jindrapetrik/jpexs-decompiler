package tests
{
   public class TestDoWhile3
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
      
      private var ch:String;
      
      public function TestDoWhile3()
      {
         method
            name "tests:TestDoWhile3/TestDoWhile3"
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
               name "tests:TestDoWhile3/run"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 2
                  localcount 1
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     jump ofs0007
            ofs0006:
                     label
            ofs0007:
                     getlocal0
                     callpropvoid QName(PrivateNamespace("tests:TestDoWhile3"),"nextChar"), 0
                     getlocal0
                     getproperty QName(PrivateNamespace("tests:TestDoWhile3"),"ch")
                     pushstring "\n"
                     equals
                     not
                     dup
                     iffalse ofs001f
                     pop
                     getlocal0
                     getproperty QName(PrivateNamespace("tests:TestDoWhile3"),"ch")
                     pushstring ""
                     equals
                     not
            ofs001f:
                     iftrue ofs0006
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
         
         private function nextChar() : void
         {
            trait method QName(PrivateNamespace("tests:TestDoWhile3"),"nextChar")
               dispid 0
               method
                  name "tests:TestDoWhile3/private/nextChar"
                  returns QName(PackageNamespace(""),"void")
                  
                  body
                     maxstack 2
                     localcount 1
                     initscopedepth 4
                     maxscopedepth 5
                     
                     code
                        getlocal0
                        pushscope
                        findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhile3"),ProtectedNamespace("tests:TestDoWhile3"),StaticProtectedNs("tests:TestDoWhile3"),PrivateNamespace("TestDoWhile3.as$0")])
                        pushstring "process next char"
                        callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDoWhile3"),ProtectedNamespace("tests:TestDoWhile3"),StaticProtectedNs("tests:TestDoWhile3"),PrivateNamespace("TestDoWhile3.as$0")]), 1
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
               findpropstrict Multiname("TestDoWhile3",[PackageNamespace("tests")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 0
               popscope
               initproperty QName(PackageNamespace("tests"),"TestDoWhile3")
               returnvoid
            end ; code
         end ; body
      end ; method
      
