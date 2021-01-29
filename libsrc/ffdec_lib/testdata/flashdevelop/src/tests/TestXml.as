package tests 
{
	public class TestXml 
	{
		
		public function run() : void
		{
			var name:String="ahoj";
			var myXML:XML=<order id="604">
					<book isbn="12345">
						<title>{name}</title>
					</book>
				</order>;

			var k:*=myXML.@id;
			var all:String=myXML.@*.toXMLString();
			k=myXML.book;
			k=myXML.book.(@isbn=="12345");

			var g:XML=new XML("<script>\n\t\t\t\t<![CDATA[\n\t\t\t\t\tfunction() {\n\t\t\t\n\t\t\t\t\t\tFBAS = {\n\t\t\t\n\t\t\t\t\t\t\tsetSWFObjectID: function( swfObjectID ) {\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tFBAS.swfObjectID = swfObjectID;\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tinit: function( opts ) {\n\t\t\t\t\t\t\t\tFB.init( FB.JSON.parse( opts ) );\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tFB.Event.subscribe( \'auth.sessionChange\', function( response ) {\n\t\t\t\t\t\t\t\t\tFBAS.updateSwfSession( response.session );\n\t\t\t\t\t\t\t\t} );\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tsetCanvasAutoResize: function( autoSize, interval ) {\n\t\t\t\t\t\t\t\tFB.Canvas.setAutoResize( autoSize, interval );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tsetCanvasSize: function( width, height ) {\n\t\t\t\t\t\t\t\tFB.Canvas.setSize( { width: width, height: height } );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tlogin: function( opts ) {\n\t\t\t\t\t\t\t\tFB.login( FBAS.handleUserLogin, FB.JSON.parse( opts ) );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\taddEventListener: function( event ) {\n\t\t\t\t\t\t\t\tFB.Event.subscribe( event, function( response ) {\n\t\t\t\t\t\t\t\t\tFBAS.getSwf().handleJsEvent( event, FB.JSON.stringify( response ) );\n\t\t\t\t\t\t\t\t} );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\thandleUserLogin: function( response ) {\n\t\t\t\t\t\t\t\tif( response.session == null ) {\n\t\t\t\t\t\t\t\t\tFBAS.updateSwfSession( null );\n\t\t\t\t\t\t\t\t\treturn;\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tif( response.perms != null ) {\n\t\t\t\t\t\t\t\t\t// user is logged in and granted some permissions.\n\t\t\t\t\t\t\t\t\t// perms is a comma separated list of granted permissions\n\t\t\t\t\t\t\t\t\tFBAS.updateSwfSession( response.session, response.perms );\n\t\t\t\t\t\t\t\t} else {\n\t\t\t\t\t\t\t\t\tFBAS.updateSwfSession( response.session );\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tlogout: function() {\n\t\t\t\t\t\t\t\tFB.logout( FBAS.handleUserLogout );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\thandleUserLogout: function( response ) {\n\t\t\t\t\t\t\t\tswf = FBAS.getSwf();\n\t\t\t\t\t\t\t\tswf.logout();\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tui: function( params ) {\n\t\t\t\t\t\t\t\tobj = FB.JSON.parse( params );\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tmethod = obj.method;\n\t\t\t\t\t\t\t\tcb = function( response ) { FBAS.getSwf().uiResponse( FB.JSON.stringify( response ), method ); }\n\t\t\t\t\t\t\t\tFB.ui( obj, cb );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tgetSession: function() {\n\t\t\t\t\t\t\t\tsession = FB.getSession();\n\t\t\t\t\t\t\t\treturn FB.JSON.stringify( session );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tgetLoginStatus: function() {\n\t\t\t\t\t\t\t\tFB.getLoginStatus( function( response ) {\n\t\t\t\t\t\t\t\t\tif( response.session ) {\n\t\t\t\t\t\t\t\t\t\tFBAS.updateSwfSession( response.session );\n\t\t\t\t\t\t\t\t\t} else {\n\t\t\t\t\t\t\t\t\t\tFBAS.updateSwfSession( null );\n\t\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t\t} );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tgetSwf: function getSwf() {\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\treturn document.getElementById( FBAS.swfObjectID );\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tupdateSwfSession: function( session, extendedPermissions ) {\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tswf = FBAS.getSwf();\n\t\t\t\t\t\t\t\textendedPermissions = ( extendedPermissions == null ) ? \'\' : extendedPermissions;\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tif( session == null ) {\n\t\t\t\t\t\t\t\t\tswf.sessionChange( null );\n\t\t\t\t\t\t\t\t} else {\n\t\t\t\t\t\t\t\t\tswf.sessionChange( FB.JSON.stringify( session ), FB.JSON.stringify( extendedPermissions.split( \',\' ) ) );\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t};\n\t\t\t\t\t}\n\t\t\t\t]]>\n\t\t\t</script>");		
		}
		
	}

}