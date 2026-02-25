package tests
{
   [MyClassTag(cls2="class 2",cls1="class 1")]
   public class TestMetadata
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
      
      [MyVarTag(var2="var 2",var1="var 1")]
      public var v:int = 5;
      
      [MyConstTag]
      public const C:int = 10;
      
      public function TestMetadata()
      {
         method
            name "tests:TestMetadata/TestMetadata"
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
      
      [MySingleTag("tag")]
      public function run() : void
      {
         trait method QName(PackageNamespace(""),"run")
            flag METADATA
            metadata "MySingleTag"
               item "" "tag"
            end ; metadata
            dispid 0
            method
               name "tests:TestMetadata/run"
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 2
                  localcount 1
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestMetadata"),ProtectedNamespace("tests:TestMetadata"),StaticProtectedNs("tests:TestMetadata"),PrivateNamespace("TestMetadata.as$0")])
                     pushstring "hello"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestMetadata"),ProtectedNamespace("tests:TestMetadata"),StaticProtectedNs("tests:TestMetadata"),PrivateNamespace("TestMetadata.as$0")]), 1
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
            findpropstrict Multiname("TestMetadata",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestMetadata")
            returnvoid
         end ; code
      end ; body
   end ; method
   
