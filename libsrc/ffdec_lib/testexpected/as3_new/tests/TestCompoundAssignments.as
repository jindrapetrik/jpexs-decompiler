package tests
{
   public class TestCompoundAssignments
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
      
      private var attr:int = 0;
      
      public function TestCompoundAssignments()
      {
         method
            name "tests:TestCompoundAssignments/TestCompoundAssignments"
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
      
      public function calc() : int
      {
         trait method QName(PackageNamespace(""),"calc")
            dispid 0
            method
               name "tests:TestCompoundAssignments/calc"
               returns QName(PackageNamespace(""),"int")
               
               body
                  maxstack 1
                  localcount 1
                  initscopedepth 4
                  maxscopedepth 5
                  
                  code
                     getlocal0
                     pushscope
                     pushbyte 5
                     returnvalue
                  end ; code
               end ; body
            end ; method
         }
         
         public function run() : void
         {
            trait method QName(PackageNamespace(""),"run")
               dispid 0
               method
                  name "tests:TestCompoundAssignments/run"
                  flag NEED_ACTIVATION
                  returns QName(PackageNamespace(""),"void")
                  
                  body
                     maxstack 4
                     localcount 3
                     initscopedepth 5
                     maxscopedepth 10
                     trait slot QName(PackageInternalNs("tests"),"t")
                        slotid 1
                        type QName(PrivateNamespace("TestCompoundAssignments.as$0"),"MyTest")
                     end ; trait
                     trait slot QName(PackageInternalNs("tests"),"b")
                        slotid 2
                        type null
                     end ; trait
                     trait slot QName(PackageInternalNs("tests"),"a")
                        slotid 3
                        type QName(PackageNamespace(""),"int")
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
                        pushbyte 10
                        pushbyte 20
                        pushbyte 30
                        newarray 3
                        coerce_a
                        setslot 2
                        getscopeobject 1
                        pushbyte 0
                        setslot 3
                        findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        pushstring "a += 5"
                        callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")]), 1
                        getscopeobject 1
                        getscopeobject 1
                        getslot 3
                        pushbyte 5
                        add
                        convert_i
                        setslot 3
                        findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        pushstring "arr[call()] = arr[call()] + 2;"
                        callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")]), 1
                        getscopeobject 1
                        getslot 2
                        getlocal0
                        callproperty QName(PackageNamespace(""),"calc"), 0
                        getscopeobject 1
                        getslot 2
                        getlocal0
                        callproperty QName(PackageNamespace(""),"calc"), 0
                        getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        pushbyte 2
                        add
                        setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        getscopeobject 1
                        findpropstrict QName(PrivateNamespace("TestCompoundAssignments.as$0"),"MyTest")
                        constructprop QName(PrivateNamespace("TestCompoundAssignments.as$0"),"MyTest"), 0
                        coerce QName(PrivateNamespace("TestCompoundAssignments.as$0"),"MyTest")
                        setslot 1
                        findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        pushstring "t.attr *= 10"
                        callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")]), 1
                        getscopeobject 1
                        getslot 1
                        getscopeobject 1
                        getslot 1
                        getproperty Multiname("attr",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        pushbyte 10
                        multiply
                        setproperty Multiname("attr",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        pushstring "attr -= 5"
                        callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")]), 1
                        getlocal0
                        getlocal0
                        getproperty QName(PrivateNamespace("tests:TestCompoundAssignments"),"attr")
                        pushbyte 5
                        subtract
                        setproperty QName(PrivateNamespace("tests:TestCompoundAssignments"),"attr")
                        findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        pushstring "arr[2] += 5"
                        callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")]), 1
                        getscopeobject 1
                        getslot 2
                        pushbyte 2
                        getscopeobject 1
                        getslot 2
                        pushbyte 2
                        getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        pushbyte 5
                        add
                        setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        pushstring "arr[call()] /= 5"
                        callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")]), 1
                        getscopeobject 1
                        getslot 2
                        getlocal0
                        callproperty QName(PackageNamespace(""),"calc"), 0
                        getscopeobject 1
                        getslot 2
                        getlocal0
                        callproperty QName(PackageNamespace(""),"calc"), 0
                        getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        pushbyte 5
                        divide
                        setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        pushstring "arr[call()][call()] &= 10;"
                        callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")]), 1
                        getscopeobject 1
                        getslot 2
                        getlocal0
                        callproperty QName(PackageNamespace(""),"calc"), 0
                        getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        getlocal0
                        callproperty QName(PackageNamespace(""),"calc"), 0
                        getscopeobject 1
                        getslot 2
                        getlocal0
                        callproperty QName(PackageNamespace(""),"calc"), 0
                        getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        getlocal0
                        callproperty QName(PackageNamespace(""),"calc"), 0
                        getproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        pushbyte 10
                        bitand
                        setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
               ofs00e2:
                        findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        pushstring "in try"
                        callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")]), 1
               ofs00e9:
                        jump ofs0106
               ofs00ed:
                        getlocal0
                        pushscope
                        getlocal1
                        pushscope
                        newcatch 0
                        dup
                        setlocal2
                        dup
                        pushscope
                        swap
                        setslot 1
                        findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        getlex Multiname("e",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        getproperty Multiname("message",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")])
                        callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestCompoundAssignments"),ProtectedNamespace("tests:TestCompoundAssignments"),StaticProtectedNs("tests:TestCompoundAssignments"),PrivateNamespace("TestCompoundAssignments.as$0")]), 1
                        popscope
                        kill 2
               ofs0106:
                        returnvoid
                     end ; code
                     try from ofs00e2 to ofs00e9 target ofs00ed type QName(PackageNamespace(""),"Error") name QName(PackageNamespace(""),"e") end
                  end ; body
               end ; method
            }
         }
      }
      
      class MyTest
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
         
         public var attr:int = 0;
         
         public function MyTest()
         {
            method
               name "TestCompoundAssignments.as$0:MyTest/MyTest"
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
               findpropstrict Multiname("TestCompoundAssignments",[PackageNamespace("tests")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 0
               popscope
               initproperty QName(PackageNamespace("tests"),"TestCompoundAssignments")
               findpropstrict Multiname("MyTest",[PrivateNamespace("TestCompoundAssignments.as$0")])
               getlex QName(PackageNamespace(""),"Object")
               pushscope
               getlex QName(PackageNamespace(""),"Object")
               newclass 1
               popscope
               initproperty QName(PrivateNamespace("TestCompoundAssignments.as$0"),"MyTest")
               returnvoid
            end ; code
         end ; body
      end ; method
      
