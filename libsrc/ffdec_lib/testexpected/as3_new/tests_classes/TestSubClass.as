package tests_classes
{
   public class TestSubClass
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
      
      public function TestSubClass()
      {
         method
            name "tests_classes:TestSubClass/TestSubClass"
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
               name "tests_classes:TestSubClass/run"
               returns null
               
               body
                  maxstack 2
                  localcount 2
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "sc", 0, 13
                     findpropstrict QName(PrivateNamespace("TestSubClass.as$0"),"SubClass")
                     constructprop QName(PrivateNamespace("TestSubClass.as$0"),"SubClass"), 0
                     coerce QName(PrivateNamespace("TestSubClass.as$0"),"SubClass")
                     setlocal1
                     getlocal1
                     pushbyte 1
                     setproperty Multiname("a_internal",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes"),PackageInternalNs("tests_classes"),PrivateNamespace("tests_classes:TestSubClass"),ProtectedNamespace("tests_classes:TestSubClass"),StaticProtectedNs("tests_classes:TestSubClass"),PrivateNamespace("TestSubClass.as$0")])
                     getlocal1
                     pushbyte 3
                     setproperty Multiname("c_public",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests_classes"),PackageInternalNs("tests_classes"),PrivateNamespace("tests_classes:TestSubClass"),ProtectedNamespace("tests_classes:TestSubClass"),StaticProtectedNs("tests_classes:TestSubClass"),PrivateNamespace("TestSubClass.as$0")])
                     returnvoid
                  end ; code
               end ; body
            end ; method
         }
      }
   }
   
   class SubClass
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
      
      internal var a_internal:int;
      
      private var b_private:int;
      
      public var c_public:int;
      
      public function SubClass()
      {
         method
            name "TestSubClass.as$0:SubClass/SubClass"
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
            findpropstrict Multiname("TestSubClass",[PackageNamespace("tests_classes")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests_classes"),"TestSubClass")
            findpropstrict Multiname("SubClass",[PrivateNamespace("TestSubClass.as$0")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 1
            popscope
            initproperty QName(PrivateNamespace("TestSubClass.as$0"),"SubClass")
            returnvoid
         end ; code
      end ; body
   end ; method
   
