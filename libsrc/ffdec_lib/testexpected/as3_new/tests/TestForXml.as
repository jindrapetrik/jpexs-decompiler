package tests
{
   public class TestForXml
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
      
      public function TestForXml()
      {
         method
            name "tests:TestForXml/TestForXml"
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
               name "tests:TestForXml/run"
               flag NEED_ACTIVATION
               returns null
               
               body
                  maxstack 4
                  localcount 7
                  initscopedepth 5
                  maxscopedepth 8
                  trait slot QName(PackageInternalNs("tests"),"i")
                     slotid 1
                     type QName(PackageNamespace(""),"int")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"c")
                     slotid 2
                     type QName(PackageNamespace(""),"int")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"name")
                     slotid 3
                     type QName(PackageNamespace(""),"String")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"myXML")
                     slotid 4
                     type QName(PackageNamespace(""),"XML")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"k")
                     slotid 5
                     type null
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"len")
                     slotid 6
                     type QName(PackageNamespace(""),"int")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"a")
                     slotid 7
                     type QName(PackageNamespace(""),"int")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"b")
                     slotid 8
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
                     pushbyte 0
                     setslot 2
                     getscopeobject 1
                     pushstring "ahoj"
                     coerce_s
                     setslot 3
                     getscopeobject 1
                     getlex QName(PackageNamespace(""),"XML")
                     pushstring "<order id=\"604\">\r\n\{24} <book isbn=\"12345\">\r\n\{24} <title>"
                     getscopeobject 1
                     getslot 3
                     esc_xelem
                     add
                     pushstring "</title>\r\n\{24} </book>\r\n\{22} </order>"
                     add
                     construct 1
                     coerce QName(PackageNamespace(""),"XML")
                     setslot 4
                     getscopeobject 1
                     pushnull
                     coerce_a
                     setslot 5
                     getscopeobject 1
                     pushbyte 5
                     setslot 6
                     getscopeobject 1
                     pushbyte 5
                     setslot 7
                     getscopeobject 1
                     pushbyte 6
                     setslot 8
                     getscopeobject 1
                     pushbyte 0
                     setslot 1
                     jump ofs00d2
            ofs004f:
                     label
                     getscopeobject 1
                     pushbyte 1
                     setslot 2
                     getscopeobject 1
                     getslot 2
                     pushbyte 2
                     ifne ofs006b
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForXml"),ProtectedNamespace("tests:TestForXml"),StaticProtectedNs("tests:TestForXml"),PrivateNamespace("TestForXml.as$0")])
                     pushstring "A"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForXml"),ProtectedNamespace("tests:TestForXml"),StaticProtectedNs("tests:TestForXml"),PrivateNamespace("TestForXml.as$0")]), 1
                     jump ofs0080
            ofs006b:
                     getscopeobject 1
                     getslot 2
                     pushbyte 3
                     ifeq ofs0079
                     jump ofs0087
            ofs0079:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForXml"),ProtectedNamespace("tests:TestForXml"),StaticProtectedNs("tests:TestForXml"),PrivateNamespace("TestForXml.as$0")])
                     pushstring "B"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForXml"),ProtectedNamespace("tests:TestForXml"),StaticProtectedNs("tests:TestForXml"),PrivateNamespace("TestForXml.as$0")]), 1
            ofs0080:
                     findpropstrict Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForXml"),ProtectedNamespace("tests:TestForXml"),StaticProtectedNs("tests:TestForXml"),PrivateNamespace("TestForXml.as$0")])
                     pushstring "C"
                     callpropvoid Multiname("trace",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForXml"),ProtectedNamespace("tests:TestForXml"),StaticProtectedNs("tests:TestForXml"),PrivateNamespace("TestForXml.as$0")]), 1
            ofs0087:
                     getscopeobject 1
                     pushbyte 0
                     setlocal3
                     getscopeobject 1
                     getslot 4
                     getproperty Multiname("book",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForXml"),ProtectedNamespace("tests:TestForXml"),StaticProtectedNs("tests:TestForXml"),PrivateNamespace("TestForXml.as$0")])
                     checkfilter
                     coerce_a
                     setlocal 4
                     getlex QName(PackageNamespace(""),"XMLList")
                     pushstring ""
                     construct 1
                     setlocal2
                     jump ofs00c1
            ofs00a1:
                     label
                     getlocal 4
                     getlocal3
                     nextvalue
                     dup
                     setlocal 5
                     dup
                     setlocal 6
                     pushwith
                     getlex MultinameA("isbn",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForXml"),ProtectedNamespace("tests:TestForXml"),StaticProtectedNs("tests:TestForXml"),PrivateNamespace("TestForXml.as$0")])
                     pushstring "12345"
                     equals
                     iffalse ofs00bc
                     getlocal2
                     getlocal3
                     getlocal 5
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestForXml"),ProtectedNamespace("tests:TestForXml"),StaticProtectedNs("tests:TestForXml"),PrivateNamespace("TestForXml.as$0")])
            ofs00bc:
                     popscope
                     kill 6
                     kill 5
            ofs00c1:
                     hasnext2 4, 3
                     iftrue ofs00a1
                     kill 4
                     kill 3
                     getlocal2
                     kill 2
                     coerce_a
                     setslot 5
            ofs00d2:
                     getscopeobject 1
                     getslot 1
                     getscopeobject 1
                     getslot 6
                     iflt ofs004f
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
            findpropstrict Multiname("TestForXml",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestForXml")
            returnvoid
         end ; code
      end ; body
   end ; method
   
