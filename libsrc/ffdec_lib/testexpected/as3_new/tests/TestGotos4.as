package tests
{
   public class TestGotos4
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
      
      public function TestGotos4()
      {
         method
            name "tests:TestGotos4/TestGotos4"
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
               name "tests:TestGotos4/run"
               flag NEED_ACTIVATION
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 3
                  localcount 3
                  initscopedepth 5
                  maxscopedepth 10
                  trait slot QName(PackageInternalNs("tests"),"a")
                     slotid 1
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
                     pushbyte 5
                     setslot 1
                     getscopeobject 1
                     getslot 1
                     pushbyte 3
                     ifngt ofs0047
                     getscopeobject 1
                     getslot 1
                     pushbyte 7
                     ifnlt ofs0047
            ofs0025:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos4"),ProtectedNamespace("tests:TestGotos4"),StaticProtectedNs("tests:TestGotos4"),PrivateNamespace("TestGotos4.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos4"),ProtectedNamespace("tests:TestGotos4"),StaticProtectedNs("tests:TestGotos4"),PrivateNamespace("TestGotos4.as$0")]), 1
            ofs002c:
                     jump ofs0040
            ofs0030:
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
                     popscope
                     kill 2
            ofs0040:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos4"),ProtectedNamespace("tests:TestGotos4"),StaticProtectedNs("tests:TestGotos4"),PrivateNamespace("TestGotos4.as$0")])
                     pushstring "B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos4"),ProtectedNamespace("tests:TestGotos4"),StaticProtectedNs("tests:TestGotos4"),PrivateNamespace("TestGotos4.as$0")]), 1
            ofs0047:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos4"),ProtectedNamespace("tests:TestGotos4"),StaticProtectedNs("tests:TestGotos4"),PrivateNamespace("TestGotos4.as$0")])
                     pushstring "return"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestGotos4"),ProtectedNamespace("tests:TestGotos4"),StaticProtectedNs("tests:TestGotos4"),PrivateNamespace("TestGotos4.as$0")]), 1
                     returnvoid
                  end ; code
                  try from ofs0025 to ofs002c target ofs0030 type QName(PackageNamespace(""),"Error") name QName(PackageNamespace(""),"error") end
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
            findpropstrict Multiname("TestGotos4",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestGotos4")
            returnvoid
         end ; code
      end ; body
   end ; method
   
