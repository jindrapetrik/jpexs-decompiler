package tests
{
   public class TestUndefined
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
      
      public function TestUndefined()
      {
         method
            name "tests:TestUndefined/TestUndefined"
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
               name "tests:TestUndefined/run"
               flag NEED_ACTIVATION
               returns null
               
               body
                  maxstack 3
                  localcount 2
                  initscopedepth 5
                  maxscopedepth 7
                  trait slot QName(PackageInternalNs("tests"),"i")
                     slotid 1
                     type QName(PackageNamespace(""),"int")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"j")
                     slotid 2
                     type QName(PackageNamespace(""),"int")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"c")
                     slotid 3
                     type QName(PackageNamespace(""),"int")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"f")
                     slotid 4
                     type QName(PackageNamespace(""),"Function")
                  end ; trait
                  
                  code
                     getlocal0
                     pushscope
                     debug 1, "+$activation", 0, 0
                     newactivation
                     dup
                     setlocal1
                     pushscope
                     getscopeobject 1
                     pushbyte 0
                     setslot 1
                     getscopeobject 1
                     pushbyte 0
                     setslot 2
                     getscopeobject 1
                     pushbyte 0
                     setslot 3
                     getscopeobject 1
                     pushbyte 5
                     getscopeobject 1
                     getslot 1
                     add
                     convert_i
                     setslot 3
                     getscopeobject 1
                     newfunction 2
                     coerce QName(PackageNamespace(""),"Function")
                     setslot 4
                     jump ofs0048
            ofs0035:
                     label
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestUndefined"),ProtectedNamespace("tests:TestUndefined"),StaticProtectedNs("tests:TestUndefined"),PrivateNamespace("TestUndefined.as$0")])
                     getscopeobject 1
                     getslot 1
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestUndefined"),ProtectedNamespace("tests:TestUndefined"),StaticProtectedNs("tests:TestUndefined"),PrivateNamespace("TestUndefined.as$0")]), 1
                     getscopeobject 1
                     getscopeobject 1
                     getslot 1
                     increment_i
                     setslot 1
            ofs0048:
                     getscopeobject 1
                     getslot 1
                     pushbyte 10
                     iflt ofs0035
                     findpropstrict Multiname("f",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestUndefined"),ProtectedNamespace("tests:TestUndefined"),StaticProtectedNs("tests:TestUndefined"),PrivateNamespace("TestUndefined.as$0")])
                     callpropvoid Multiname("f",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestUndefined"),ProtectedNamespace("tests:TestUndefined"),StaticProtectedNs("tests:TestUndefined"),PrivateNamespace("TestUndefined.as$0")]), 0
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
            findpropstrict Multiname("TestUndefined",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestUndefined")
            returnvoid
         end ; code
      end ; body
   end ; method
   
