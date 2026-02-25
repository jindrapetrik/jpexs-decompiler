package tests
{
   public class TestXml
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
      
      public function TestXml()
      {
         method
            name "tests:TestXml/TestXml"
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
               name "tests:TestXml/run"
               flag NEED_ACTIVATION
               returns QName(PackageNamespace(""),"void")
               
               body
                  maxstack 5
                  localcount 7
                  initscopedepth 5
                  maxscopedepth 8
                  trait slot QName(PackageInternalNs("tests"),"list")
                     slotid 1
                     type TypeName(QName(PackageNamespace("__AS3__.vec"),"Vector")<QName(PackageNamespace(""),"int")>)
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"i")
                     slotid 2
                     type QName(PackageNamespace(""),"int")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"g")
                     slotid 3
                     type QName(PackageNamespace(""),"XML")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"testCdata")
                     slotid 4
                     type QName(PackageNamespace(""),"XML")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"testComment")
                     slotid 5
                     type QName(PackageNamespace(""),"XML")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"xtaga")
                     slotid 6
                     type QName(PackageNamespace(""),"String")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"xtagb")
                     slotid 7
                     type QName(PackageNamespace(""),"String")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"xattrname")
                     slotid 8
                     type QName(PackageNamespace(""),"String")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"xattrval")
                     slotid 9
                     type QName(PackageNamespace(""),"String")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"xcontent")
                     slotid 10
                     type QName(PackageNamespace(""),"String")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"xxx")
                     slotid 11
                     type QName(PackageNamespace(""),"XML")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"m")
                     slotid 12
                     type QName(PackageNamespace(""),"XMLList")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"name")
                     slotid 13
                     type QName(PackageNamespace(""),"String")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"myXML")
                     slotid 14
                     type QName(PackageNamespace(""),"XML")
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"k")
                     slotid 15
                     type null
                  end ; trait
                  trait slot QName(PackageInternalNs("tests"),"all")
                     slotid 16
                     type QName(PackageNamespace(""),"String")
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
                     pushnull
                     coerce TypeName(QName(PackageNamespace("__AS3__.vec"),"Vector")<QName(PackageNamespace(""),"int")>)
                     setslot 1
                     getscopeobject 1
                     pushbyte 0
                     setslot 2
                     getscopeobject 1
                     pushnull
                     coerce QName(PackageNamespace(""),"XML")
                     setslot 3
                     getscopeobject 1
                     pushnull
                     coerce QName(PackageNamespace(""),"XML")
                     setslot 4
                     getscopeobject 1
                     pushnull
                     coerce QName(PackageNamespace(""),"XML")
                     setslot 5
                     getscopeobject 1
                     pushnull
                     coerce_s
                     setslot 6
                     getscopeobject 1
                     pushnull
                     coerce_s
                     setslot 7
                     getscopeobject 1
                     pushnull
                     coerce_s
                     setslot 8
                     getscopeobject 1
                     pushnull
                     coerce_s
                     setslot 9
                     getscopeobject 1
                     pushnull
                     coerce_s
                     setslot 10
                     getscopeobject 1
                     pushnull
                     coerce QName(PackageNamespace(""),"XML")
                     setslot 11
                     getscopeobject 1
                     pushnull
                     coerce QName(PackageNamespace(""),"XMLList")
                     setslot 12
                     getscopeobject 1
                     pushstring "ahoj"
                     coerce_s
                     setslot 13
                     getscopeobject 1
                     getlex QName(PackageNamespace(""),"XML")
                     pushstring "<order id=\"604\">\r\n\t\t\t\t\t<book isbn=\"12345\">\r\n\t\t\t\t\t\t<title>"
                     getscopeobject 1
                     getslot 13
                     esc_xelem
                     add
                     pushstring "</title>\r\n\t\t\t\t\t</book>\r\n\t\t\t\t</order>"
                     add
                     construct 1
                     coerce QName(PackageNamespace(""),"XML")
                     setslot 14
                     getscopeobject 1
                     getscopeobject 1
                     getslot 14
                     getproperty MultinameA("id",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestXml"),ProtectedNamespace("tests:TestXml"),StaticProtectedNs("tests:TestXml"),PrivateNamespace("TestXml.as$0")])
                     coerce_a
                     setslot 15
                     getscopeobject 1
                     getscopeobject 1
                     getslot 14
                     getproperty MultinameA(null,[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestXml"),ProtectedNamespace("tests:TestXml"),StaticProtectedNs("tests:TestXml"),PrivateNamespace("TestXml.as$0")])
                     callproperty Multiname("toXMLString",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestXml"),ProtectedNamespace("tests:TestXml"),StaticProtectedNs("tests:TestXml"),PrivateNamespace("TestXml.as$0")]), 0
                     coerce_s
                     setslot 16
                     getscopeobject 1
                     getscopeobject 1
                     getslot 14
                     getproperty Multiname("book",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestXml"),ProtectedNamespace("tests:TestXml"),StaticProtectedNs("tests:TestXml"),PrivateNamespace("TestXml.as$0")])
                     coerce_a
                     setslot 15
                     getscopeobject 1
                     pushbyte 0
                     setlocal3
                     getscopeobject 1
                     getslot 14
                     getproperty Multiname("book",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestXml"),ProtectedNamespace("tests:TestXml"),StaticProtectedNs("tests:TestXml"),PrivateNamespace("TestXml.as$0")])
                     checkfilter
                     coerce_a
                     setlocal 4
                     getlex QName(PackageNamespace(""),"XMLList")
                     pushstring ""
                     construct 1
                     setlocal2
                     jump ofs00d3
            ofs00b3:
                     label
                     getlocal 4
                     getlocal3
                     nextvalue
                     dup
                     setlocal 5
                     dup
                     setlocal 6
                     pushwith
                     getlex MultinameA("isbn",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestXml"),ProtectedNamespace("tests:TestXml"),StaticProtectedNs("tests:TestXml"),PrivateNamespace("TestXml.as$0")])
                     pushstring "12345"
                     equals
                     iffalse ofs00ce
                     getlocal2
                     getlocal3
                     getlocal 5
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestXml"),ProtectedNamespace("tests:TestXml"),StaticProtectedNs("tests:TestXml"),PrivateNamespace("TestXml.as$0")])
            ofs00ce:
                     popscope
                     kill 6
                     kill 5
            ofs00d3:
                     hasnext2 4, 3
                     iftrue ofs00b3
                     kill 4
                     kill 3
                     getlocal2
                     kill 2
                     coerce_a
                     setslot 15
                     getscopeobject 1
                     getlex QName(PackageNamespace("__AS3__.vec"),"Vector")
                     getlex QName(PackageNamespace(""),"int")
                     applytype 1
                     construct 0
                     coerce TypeName(QName(PackageNamespace("__AS3__.vec"),"Vector")<QName(PackageNamespace(""),"int")>)
                     setslot 1
                     getscopeobject 1
                     findpropstrict QName(PackageNamespace(""),"int")
                     getlex QName(PackageNamespace(""),"Math")
                     callproperty QName(PackageNamespace(""),"random"), 0
                     callproperty QName(PackageNamespace(""),"int"), 1
                     convert_i
                     setslot 2
                     getscopeobject 1
                     getslot 1
                     getscopeobject 1
                     getslot 2
                     pushbyte 0
                     setlocal3
                     getscopeobject 1
                     getslot 14
                     getproperty Multiname("book",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestXml"),ProtectedNamespace("tests:TestXml"),StaticProtectedNs("tests:TestXml"),PrivateNamespace("TestXml.as$0")])
                     checkfilter
                     coerce_a
                     setlocal 4
                     getlex QName(PackageNamespace(""),"XMLList")
                     pushstring ""
                     construct 1
                     setlocal2
                     jump ofs0146
            ofs0121:
                     label
                     getlocal 4
                     getlocal3
                     nextvalue
                     dup
                     setlocal 5
                     dup
                     setlocal 6
                     pushwith
                     getlex MultinameA("isbn",[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestXml"),ProtectedNamespace("tests:TestXml"),StaticProtectedNs("tests:TestXml"),PrivateNamespace("TestXml.as$0")])
                     getscopeobject 1
                     getslot 2
                     pushbyte 1
                     add
                     equals
                     iffalse ofs0141
                     getlocal2
                     getlocal3
                     getlocal 5
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestXml"),ProtectedNamespace("tests:TestXml"),StaticProtectedNs("tests:TestXml"),PrivateNamespace("TestXml.as$0")])
            ofs0141:
                     popscope
                     kill 6
                     kill 5
            ofs0146:
                     hasnext2 4, 3
                     iftrue ofs0121
                     kill 4
                     kill 3
                     getlocal2
                     kill 2
                     setproperty MultinameL([PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestXml"),ProtectedNamespace("tests:TestXml"),StaticProtectedNs("tests:TestXml"),PrivateNamespace("TestXml.as$0")])
                     getscopeobject 1
                     getlex QName(PackageNamespace(""),"XML")
                     pushstring "<script>\n\t\t\t\t<![CDATA[\n\t\t\t\t\tfunction() {\n\t\t\t\n\t\t\t\t\t\tFBAS = {\n\t\t\t\n\t\t\t\t\t\t\tsetSWFObjectID: function( swfObjectID ) {\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tFBAS.swfObjectID = swfObjectID;\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tinit: function( opts ) {\n\t\t\t\t\t\t\t\tFB.init( FB.JSON.parse( opts ) );\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tFB.Event.subscribe( \'auth.sessionChange\', function( response ) {\n\t\t\t\t\t\t\t\t\tFBAS.updateSwfSession( response.session );\n\t\t\t\t\t\t\t\t} );\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tsetCanvasAutoResize: function( autoSize, interval ) {\n\t\t\t\t\t\t\t\tFB.Canvas.setAutoResize( autoSize, interval );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tsetCanvasSize: function( width, height ) {\n\t\t\t\t\t\t\t\tFB.Canvas.setSize( { width: width, height: height } );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tlogin: function( opts ) {\n\t\t\t\t\t\t\t\tFB.login( FBAS.handleUserLogin, FB.JSON.parse( opts ) );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\taddEventListener: function( event ) {\n\t\t\t\t\t\t\t\tFB.Event.subscribe( event, function( response ) {\n\t\t\t\t\t\t\t\t\tFBAS.getSwf().handleJsEvent( event, FB.JSON.stringify( response ) );\n\t\t\t\t\t\t\t\t} );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\thandleUserLogin: function( response ) {\n\t\t\t\t\t\t\t\tif( response.session == null ) {\n\t\t\t\t\t\t\t\t\tFBAS.updateSwfSession( null );\n\t\t\t\t\t\t\t\t\treturn;\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tif( response.perms != null ) {\n\t\t\t\t\t\t\t\t\t// user is logged in and granted some permissions.\n\t\t\t\t\t\t\t\t\t// perms is a comma separated list of granted permissions\n\t\t\t\t\t\t\t\t\tFBAS.updateSwfSession( response.session, response.perms );\n\t\t\t\t\t\t\t\t} else {\n\t\t\t\t\t\t\t\t\tFBAS.updateSwfSession( response.session );\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tlogout: function() {\n\t\t\t\t\t\t\t\tFB.logout( FBAS.handleUserLogout );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\thandleUserLogout: function( response ) {\n\t\t\t\t\t\t\t\tswf = FBAS.getSwf();\n\t\t\t\t\t\t\t\tswf.logout();\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tui: function( params ) {\n\t\t\t\t\t\t\t\tobj = FB.JSON.parse( params );\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tmethod = obj.method;\n\t\t\t\t\t\t\t\tcb = function( response ) { FBAS.getSwf().uiResponse( FB.JSON.stringify( response ), method ); }\n\t\t\t\t\t\t\t\tFB.ui( obj, cb );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tgetSession: function() {\n\t\t\t\t\t\t\t\tsession = FB.getSession();\n\t\t\t\t\t\t\t\treturn FB.JSON.stringify( session );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tgetLoginStatus: function() {\n\t\t\t\t\t\t\t\tFB.getLoginStatus( function( response ) {\n\t\t\t\t\t\t\t\t\tif( response.session ) {\n\t\t\t\t\t\t\t\t\t\tFBAS.updateSwfSession( response.session );\n\t\t\t\t\t\t\t\t\t} else {\n\t\t\t\t\t\t\t\t\t\tFBAS.updateSwfSession( null );\n\t\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t\t} );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tgetSwf: function getSwf() {\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\treturn document.getElementById( FBAS.swfObjectID );\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tupdateSwfSession: function( session, extendedPermissions ) {\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tswf = FBAS.getSwf();\n\t\t\t\t\t\t\t\textendedPermissions = ( extendedPermissions == null ) ? \'\' : extendedPermissions;\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tif( session == null ) {\n\t\t\t\t\t\t\t\t\tswf.sessionChange( null );\n\t\t\t\t\t\t\t\t} else {\n\t\t\t\t\t\t\t\t\tswf.sessionChange( FB.JSON.stringify( session ), FB.JSON.stringify( extendedPermissions.split( \',\' ) ) );\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t};\n\t\t\t\t\t}\n\t\t\t\t]]>\n\t\t\t</script>"
                     construct 1
                     coerce QName(PackageNamespace(""),"XML")
                     setslot 3
                     getscopeobject 1
                     getlex QName(PackageNamespace(""),"XML")
                     pushstring "<![CDATA[\r\n\t\t\t\thello from cdata;\r\n\t\t\t\tfunction(){\r\n\t\t\t\t\there some code;\r\n\t\t\t\t}\r\n\t\t\t]]>"
                     construct 1
                     coerce QName(PackageNamespace(""),"XML")
                     setslot 4
                     getscopeobject 1
                     getlex QName(PackageNamespace(""),"XML")
                     pushstring "<!-- myXML comment-->"
                     construct 1
                     coerce QName(PackageNamespace(""),"XML")
                     setslot 5
                     getscopeobject 1
                     pushstring "a"
                     coerce_s
                     setslot 6
                     getscopeobject 1
                     pushstring "b"
                     coerce_s
                     setslot 7
                     getscopeobject 1
                     pushstring "attr"
                     coerce_s
                     setslot 8
                     getscopeobject 1
                     pushstring "value"
                     coerce_s
                     setslot 9
                     getscopeobject 1
                     pushstring "content"
                     coerce_s
                     setslot 10
                     getscopeobject 1
                     getlex QName(PackageNamespace(""),"XML")
                     pushstring "<"
                     getscopeobject 1
                     getslot 6
                     add
                     pushstring ">\r\n\{28} <"
                     add
                     getscopeobject 1
                     getslot 7
                     add
                     pushstring ">\r\n\{32} <ul"
                     add
                     pushstring ">\r\n\{34} <li"
                     add
                     pushstring ">Item 1</li>\{28} \r\n\{34} <li"
                     add
                     pushstring " "
                     add
                     getscopeobject 1
                     getslot 8
                     add
                     pushstring "=\"val\" attr2=\""
                     add
                     getscopeobject 1
                     getslot 9
                     esc_xattr
                     add
                     pushstring "\">Item 2: "
                     add
                     getscopeobject 1
                     getslot 10
                     esc_xelem
                     add
                     pushstring "</li>\r\n\{34} <?processinstr testvalue ?>\r\n\{34} <!-- \r\n\{34} comment\r\n\{34} -->\r\n\{32} </ul>\r\n\{28} </"
                     add
                     getscopeobject 1
                     getslot 7
                     add
                     pushstring ">\r\n\{24} </"
                     add
                     getscopeobject 1
                     getslot 6
                     add
                     pushstring ">"
                     add
                     construct 1
                     coerce QName(PackageNamespace(""),"XML")
                     setslot 11
                     getscopeobject 1
                     getscopeobject 1
                     getslot 14
                     getproperty Multiname(null,[PackageNamespace(""),Namespace("http://adobe.com/AS3/2006/builtin"),PackageNamespace("tests"),PackageInternalNs("tests"),PrivateNamespace("tests:TestXml"),ProtectedNamespace("tests:TestXml"),StaticProtectedNs("tests:TestXml"),PrivateNamespace("TestXml.as$0")])
                     coerce QName(PackageNamespace(""),"XMLList")
                     setslot 12
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
            findpropstrict Multiname("TestXml",[PackageNamespace("tests")])
            getlex QName(PackageNamespace(""),"Object")
            pushscope
            getlex QName(PackageNamespace(""),"Object")
            newclass 0
            popscope
            initproperty QName(PackageNamespace("tests"),"TestXml")
            returnvoid
         end ; code
      end ; body
   end ; method
   
