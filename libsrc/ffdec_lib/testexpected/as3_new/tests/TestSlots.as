package tests
{
   public class TestSlots
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
      
      public function TestSlots()
      {
         method
            name "tests:TestSlots/TestSlots"
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
               name "tests:TestSlots/run"
               flag NEED_ACTIVATION
               returns null
               
               body
                  maxstack 4
                  localcount 2
                  initscopedepth 5
                  maxscopedepth 7
                  trait slot QName(PackageInternalNs("tests"),"i")
                     slotid 1
                     type QName(PackageNamespace(""),"int")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"f")
                     slotid 2
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
                     pushbyte 1
                     setslot 1
                     getscopeobject 1
                     newfunction 2
                     coerce QName(PackageNamespace(""),"Function")
                     setslot 2
                     getscopeobject 1
                     pushbyte 0
                     setslot 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSlots"),ProtectedNamespace("tests:TestSlots"),StaticProtectedNs("tests:TestSlots"),PrivateNamespace("TestSlots.as$0")])
                     getscopeobject 1
                     getslot 1
                     dup
                     increment_i
                     convert_i
                     getscopeobject 1
                     swap
                     setslot 1
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSlots"),ProtectedNamespace("tests:TestSlots"),StaticProtectedNs("tests:TestSlots"),PrivateNamespace("TestSlots.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSlots"),ProtectedNamespace("tests:TestSlots"),StaticProtectedNs("tests:TestSlots"),PrivateNamespace("TestSlots.as$0")])
                     getscopeobject 1
                     getslot 1
                     dup
                     decrement_i
                     convert_i
                     getscopeobject 1
                     swap
                     setslot 1
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSlots"),ProtectedNamespace("tests:TestSlots"),StaticProtectedNs("tests:TestSlots"),PrivateNamespace("TestSlots.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSlots"),ProtectedNamespace("tests:TestSlots"),StaticProtectedNs("tests:TestSlots"),PrivateNamespace("TestSlots.as$0")])
                     getscopeobject 1
                     getslot 1
                     increment_i
                     dup
                     convert_i
                     getscopeobject 1
                     swap
                     setslot 1
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSlots"),ProtectedNamespace("tests:TestSlots"),StaticProtectedNs("tests:TestSlots"),PrivateNamespace("TestSlots.as$0")]), 1
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSlots"),ProtectedNamespace("tests:TestSlots"),StaticProtectedNs("tests:TestSlots"),PrivateNamespace("TestSlots.as$0")])
                     getscopeobject 1
                     getslot 1
                     decrement_i
                     dup
                     convert_i
                     getscopeobject 1
                     swap
                     setslot 1
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestSlots"),ProtectedNamespace("tests:TestSlots"),StaticProtectedNs("tests:TestSlots"),PrivateNamespace("TestSlots.as$0")]), 1
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
            findpropstrict Multiname("TestSlots",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestSlots")
            returnvoid
         end ; code
      end ; body
   end ; method
   
