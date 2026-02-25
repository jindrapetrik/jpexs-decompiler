package tests
{
   public class TestDotParent
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
      
      public function TestDotParent()
      {
         method
            name "tests:TestDotParent/TestDotParent"
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
               name "tests:TestDotParent/run"
               flag NEED_ACTIVATION
               returns null
               
               body
                  maxstack 4
                  localcount 9
                  initscopedepth 5
                  maxscopedepth 8
                  trait slot QName(PackageInternalNs("tests"),"d")
                     slotid 1
                     type null
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"k")
                     slotid 2
                     type null
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"g")
                     slotid 3
                     type null
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
                     pushundefined
                     coerce_a
                     setslot 1
                     getscopeobject 1
                     pushundefined
                     coerce_a
                     setslot 2
                     getscopeobject 1
                     pushundefined
                     coerce_a
                     setslot 3
                     getscopeobject 1
                     findpropstrict QName(PrivateNamespace("TestDotParent.as$0"),"TestClass1")
                     constructprop QName(PrivateNamespace("TestDotParent.as$0"),"TestClass1"), 0
                     coerce_a
                     setslot 1
                     getscopeobject 1
                     pushnull
                     coerce_a
                     setslot 2
                     pushbyte 0
                     setlocal3
                     getscopeobject 1
                     getslot 2
                     checkfilter
                     coerce_a
                     setlocal 4
                     getlex QName(PackageNamespace(""),"XMLList")
                     pushstring ""
                     construct 1
                     setlocal2
                     jump ofs0076
            ofs0043:
                     label
                     getlocal 4
                     getlocal3
                     nextvalue
                     dup
                     setlocal 5
                     dup
                     setlocal 6
                     pushwith
                     getscopeobject 1
                     getslot 1
                     dup
                     setlocal 7
                     getproperty Multiname("attrib",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDotParent"),ProtectedNamespace("tests:TestDotParent"),StaticProtectedNs("tests:TestDotParent"),PrivateNamespace("TestDotParent.as$0")])
                     increment
                     setlocal 8
                     getlocal 7
                     getlocal 8
                     setproperty Multiname("attrib",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDotParent"),ProtectedNamespace("tests:TestDotParent"),StaticProtectedNs("tests:TestDotParent"),PrivateNamespace("TestDotParent.as$0")])
                     kill 8
                     kill 7
                     pushbyte 0
                     iffalse ofs0071
                     getlocal2
                     getlocal3
                     getlocal 5
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDotParent"),ProtectedNamespace("tests:TestDotParent"),StaticProtectedNs("tests:TestDotParent"),PrivateNamespace("TestDotParent.as$0")])
            ofs0071:
                     popscope
                     kill 6
                     kill 5
            ofs0076:
                     hasnext2 4, 3
                     iftrue ofs0043
                     kill 4
                     kill 3
                     getlocal2
                     kill 2
                     pop
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDotParent"),ProtectedNamespace("tests:TestDotParent"),StaticProtectedNs("tests:TestDotParent"),PrivateNamespace("TestDotParent.as$0")])
                     pushstring "between"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDotParent"),ProtectedNamespace("tests:TestDotParent"),StaticProtectedNs("tests:TestDotParent"),PrivateNamespace("TestDotParent.as$0")]), 1
                     getscopeobject 1
                     pushbyte 0
                     setlocal3
                     getscopeobject 1
                     getslot 2
                     checkfilter
                     coerce_a
                     setlocal 4
                     getlex QName(PackageNamespace(""),"XMLList")
                     pushstring ""
                     construct 1
                     setlocal2
                     jump ofs00d7
            ofs00a4:
                     label
                     getlocal 4
                     getlocal3
                     nextvalue
                     dup
                     setlocal 5
                     dup
                     setlocal 6
                     pushwith
                     getscopeobject 1
                     getslot 1
                     dup
                     setlocal 7
                     getproperty Multiname("attrib",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDotParent"),ProtectedNamespace("tests:TestDotParent"),StaticProtectedNs("tests:TestDotParent"),PrivateNamespace("TestDotParent.as$0")])
                     increment
                     setlocal 8
                     getlocal 7
                     getlocal 8
                     setproperty Multiname("attrib",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDotParent"),ProtectedNamespace("tests:TestDotParent"),StaticProtectedNs("tests:TestDotParent"),PrivateNamespace("TestDotParent.as$0")])
                     kill 8
                     kill 7
                     pushbyte 0
                     iffalse ofs00d2
                     getlocal2
                     getlocal3
                     getlocal 5
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDotParent"),ProtectedNamespace("tests:TestDotParent"),StaticProtectedNs("tests:TestDotParent"),PrivateNamespace("TestDotParent.as$0")])
            ofs00d2:
                     popscope
                     kill 6
                     kill 5
            ofs00d7:
                     hasnext2 4, 3
                     iftrue ofs00a4
                     kill 4
                     kill 3
                     getlocal2
                     kill 2
                     coerce_a
                     setslot 3
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDotParent"),ProtectedNamespace("tests:TestDotParent"),StaticProtectedNs("tests:TestDotParent"),PrivateNamespace("TestDotParent.as$0")])
                     pushstring "end"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestDotParent"),ProtectedNamespace("tests:TestDotParent"),StaticProtectedNs("tests:TestDotParent"),PrivateNamespace("TestDotParent.as$0")]), 1
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
            name "TestDotParent.as$0:TestClass1/TestClass1"
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
            findpropstrict Multiname("TestDotParent",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestDotParent")
            findpropstrict Multiname("TestClass1",[PrivateNamespace("TestDotParent.as$0")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 1
            popscope
            initproperty QName(PrivateNamespace("TestDotParent.as$0"),"TestClass1")
            returnvoid
         end ; code
      end ; body
   end ; method
   
