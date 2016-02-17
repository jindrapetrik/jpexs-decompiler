package classes
{

	import flash.utils.Dictionary;
	import classes.myInternal;
	import flash.errors.EOFError;
	import flash.events.Event;
	public class Test implements MyInterface2
	{
		private var testPriv:int=5;
		protected var testProt:int=9;
		public var t3:TestClass3;
		public static const p:int=8;
		private namespace n="nazdar";
		private var mc:MoreClass;

		public function testHello()
		{
			trace("hello");
		}
		
		public function interfaceMethod(a:int):int {
			return a+1;
		}
		
		public function interface2Method(a:int,b:int):int {
			return a + b;
		}

		public function testIncDec()
		{
			var a=5;
			var b=0;
			trace("++var");
			b=++a;
			trace("var++");
			b=a++;
			trace("--var");
			b=--a;
			trace("var--");
			b=a--;
			var c=[1, 2, 3, 4, 5];
			trace("++arr");
			b=++c[2];
			trace("arr++");
			b=c[2]++;
			trace("--arr");
			b=--c[2];
			trace("arr--");
			b=c[2]--;

			var d=new TestClass1();
			trace("++property");
			trace(++d.attrib);
			trace("property++");
			trace(d.attrib++);
			trace("--property");
			trace(--d.attrib);
			trace("property--");
			trace(d.attrib--);

			trace("arr[e++]");

			var chars:Array=new Array(36);
			var index:uint=0;

			chars[index++]=5;

			trace("arr[++e]");
			chars[++index]=5;
			return;
		}

		public function testDoWhile()
		{
			var a=8;
			do
			{
				trace("a=" + a);
				a++;
			} while (a < 20);
			return;
		}

		public function testInnerTry()
		{
			try
			{
				try
				{
					trace("try body 1");
				}
				catch (e:DefinitionError)
				{
					trace("catched DefinitionError");
				}
				trace("after try 1");
			}
			catch (e:Error)
			{
				trace("catched Error");
			}
			finally
			{
				trace("finally block");
			}
		}

		public function testWhileContinue()
		{
			var a=5;
			while (true)
			{
				if (a == 9)
				{
					if (a == 8)
					{
						continue;
					}
					if (a == 9)
					{
						break;
					}
					trace("hello 1");
				}
				trace("hello2");
			}
			return;
		}

		public function testPrecedence()
		{
			var a=0;
			a=(5 + 6) * 7;
			a=5 * (2 + 3);
			a=5 + 6 * 7;
			a=5 * 2 + 2;
			a=5 * (25 % 3);
			a=5 % (24 * 307);
			a=1 / (2 / 3);
			a=1 / (2 * 3);
			a=1 * 2 * 3;
			a=1 * 2 / 3;
			trace("a=" + a);
			return;
		}

		public function testStrings()
		{
			trace("hello");
			trace("quotes:\"hello!\"");
			trace("backslash: \\ ");
			trace("single quotes: \'hello!\'");
			trace("new line \r\n hello!");
		}

		public function testContinueLevels()
		{
			var a=5;
			loop123: switch (a)
			{
				case 57 * a:
					trace("fiftyseven multiply a");
					var b=0;
					while (b < 50)
					{
						if (b == 10)
						{
							break;
						}
						if (b == 15)
						{
							break loop123;
						}
						b=b + 1;
					}
					break;
				case 13:
					trace("thirteen");
				case 14:
					trace("fourteen");
					break;
				case 89:
					trace("eightynine");
					break;
				default:
					trace("default clause");
			}

			loop182: for (var c=0; c < 8; c=c + 1)
			{

				loop165: for (var d=0; d < 25; d++)
				{

					for (var e=0; e < 50; e++)
					{
						if (e == 9)
						{
							break loop165;
						}
						if (e == 20)
						{
							continue loop182;
						}
						if (e == 8)
						{
							break;
						}
						break loop182;
					}
				}
				trace("hello");
			}
import classes.TestNs;

		}

		public function testSwitchDefault()
		{
			var a=5;
			switch (a)
			{
				case 57 * a:
					trace("fiftyseven multiply a");
					break;
				case 13:
					trace("thirteen");
				case 14:
					trace("fourteen");
					break;
				case 89:
					trace("eightynine");
					break;
				default:
					trace("default clause");
			}
		}

		public function testMultipleCondition()
		{
			var a=5;
			var b=8;
			var c=9;
			if ((a <= 4 || b <= 8) && c == 7)
			{
				trace("onTrue");
			}
			else
			{
				trace("onFalse");
			}
		}

		public function testForBreak()
		{
			for (var a=0; a < 10; a++)
			{
				if (a == 5)
				{
					break;
				}
				trace("hello:" + a);
			}
		}

		public function testIf()
		{
			var a=5;
			if (a == 7)
			{
				trace("onTrue");
			}
		}

		public function testIfElse()
		{
			var a=5;
			if (a == 7)
			{
				trace("onTrue");
			}
			else
			{
				trace("onFalse");
			}
		}

		public function testFor()
		{
			for (var a=0; a < 10; a++)
			{
				trace("a=" + a);
			}
		}

		public function testForContinue()
		{
			for (var a=0; a < 10; a=a + 1)
			{
				if (a == 9)
				{
					if (a == 5)
					{
						trace("part1");
						continue;
					}
					trace("a=" + a);
					if (a == 7)
					{
						trace("part2");
						continue;
					}
					trace("part3");
				}
				else
				{
					trace("part4");
				}
				trace("part5");
			}
		}

		public function testTry()
		{
			var i:int;
			i=7;
			try
			{
				trace("try body");
			}
			catch (e:DefinitionError)
			{
				trace("catched DefinitionError");
			}
			catch (e:Error)
			{
				trace("Error message:" + e.message);
				trace("Stacktrace:" + e.getStackTrace());
			}
			finally
			{
				trace("Finally part");
			}
			trace("end");
		}

		public function testTryShouldHaveCatchOrFinally() {
			try
			{
				trace("try body");
			}
			finally
			{
			}
		}

		public function testSwitch()
		{
			var a=5;
			switch (a)
			{
				case 57 * a:
					trace("fiftyseven multiply a");
					break;
				case 13:
					trace("thirteen");
				case 14:
					trace("fourteen");
					break;
				case 89:
					trace("eightynine");
					break;
			}
		}

		public function testTernarOperator()
		{
			var a=5;
			var b=4;
			var c=4;
			var d=78;
			var e=(a == b) ? ((c == d) ? 1 : 7) : 3;
			trace("e=" + e);
		}

		public function testInnerIf()
		{
			var a=5;
			var b=4;
			if (a == 5)
			{
				if (b == 6)
				{
					trace("b==6");
				}
				else
				{
					trace("b!=6");
				}
			}
			else
			{
				if (b == 7)
				{
					trace("b==7");
				}
				else
				{
					trace("b!=7");
				}
			}
			trace("end");
		}

		public function testVector()
		{
			var v:Vector.<String>=new Vector.<String>();
			v.push("hello");
			v[0]="hi";
			v[5 * 8 - 39]="hi2";
			trace(v[0]);
		}

		public function testProperty()
		{
			var d=new TestClass1();
			var k=7 + 8;
			if (k == 15)
			{
				d.method(d.attrib * 5);
			}
		}

		public function testRest(firstp:int, ... restval):int
		{
			trace("firstRest:" + restval[0]);
			return firstp;
		}

		public function testParamNames(firstp:int, secondp:int, thirdp:int):int
		{
			return firstp + secondp + thirdp;
		}

		public function testForEach()
		{
			var list:Array;
			list=new Array();
			list[0]="first";
			list[1]="second";
			list[2]="third";
			for each (var item in list)
			{
				trace("item #" + item);
			}
		}

		public function testForEachObjectArray()
		{
			var list:Array;
			list=new Array();
			list[0]="first";
			list[1]="second";
			list[2]="third";
			var test:Array;
			test=new Array();
			test[0]=0;
			for each (test[0] in list)
			{
				trace("item #" + test[0]);
			}
		}

		public function testForEachObjectAttribute()
		{
			var list:Array;
			list=new Array();
			list[0]="first";
			list[1]="second";
			list[2]="third";
			for each (testPriv in list)
			{
				trace("item #" + testPriv);
			}
		}

		public function testParamsCount(firstp:int, secondp:int, thirdp:int):int
		{
			return firstp;
		}

		public function testInlineFunctions()
		{
			var first:String="value1";
			var traceParameter:Function=function(aParam:String):String
			{
				var second:String="value2";
				second=second + "cc";
				var traceParam2:Function=function(bParam:String):String
				{
					trace(bParam + "," + aParam);
					return first + second + aParam + bParam;
				}
				trace(second);
				traceParam2(aParam);
				return first;
			};
			traceParameter("hello");
		}

		public function testMissingDefault()
		{
			var jj:int=1;
			switch (jj)
			{
				case 1:
					jj=1;
					break;
				case 2:
					jj=2;
					break;
				default:
					jj=3;
			}
		}

		private function traceIt(s:String)
		{
			trace(s);
		}

		private static var counter:int;

		private function getCounter():int
		{
			counter++;
			return counter;
		}

		public function testChainedAssignments()
		{
			var a:int;
			var b:int;
			var c:int;
			var d:int;
			d=c=b=a=5;
			var e:TestClass2=TestClass2.createMe("test");
			e.attrib1=e.attrib2=e.attrib3=getCounter();
			traceIt(e.toString());
		}

		private function testFinallyZeroJump(param1:String):String
		{
			var str:String=param1;
			try
			{
				return str;
			}
			catch (e:Error)
			{
				trace("error is :" + e.message);
			}
			finally
			{
				trace("hi ");
				if (5 == 4)
				{
					return str;
				}
				else
				{
					return "hu" + str;
				}
			}
		}

		public function testInnerFunctions(a:String)
		{
			var k:int=5;
			if (k == 6)
			{
				var s:int=8;
			}
			function innerFunc(b:String)
			{
				trace(b);
			}
			innerFunc(a);
		}

		public function testDeclarations()
		{
			var vall:*;
			var vstr:String;
			var vint:int;
			var vuint:uint;
			var vclass:TestClass1;
			var vnumber:Number;
			var vobject:Object;

			vall=6;
			vstr="hello";
			vuint=7;
			vint=-4;
			vclass=new TestClass1();
			vnumber=0.5;
			vnumber=6;
			vobject=vclass;
		}

		public function testForIn()
		{
			var dic:Dictionary;
			var item:Object;
			for (item in dic)
			{
				trace(item);
			}
			for each (item in dic)
			{
				trace(item);
			}
		}

		myInternal function namespacedFunc()
		{
			trace("hello");
		}

		function unnamespacedFunc()
		{
			trace("hello2");
		}

		function getNamespace():Namespace
		{
			return myInternal;
		}

		function getName():String
		{
			return "unnamespacedFunc";
		}

		function testNames()
		{
			var ns=getNamespace();
			var name=getName();
			var a=ns::unnamespacedFunc();
			var b=ns::[name];
			trace(b.c);
			var c=myInternal::neco;
		}

		function testComplexExpressions()
		{
			var i:int;
			var j:int;
			j=(i=i + (i+=i++));
		}

		function testExpressions()
		{
			var i:int=5;
			var j:int=5;
			var arr:Array;

			if ((i=i/=2) == 1 || i == 2)
			{
				arguments.concat(i);
			}
			else if (i == 0)
			{
				i=j++;
			}
			else
			{
				arr[0]();
			}

			return i == 0;
		}

		public function testArguments(a:int):String
		{
			return arguments[0];
		}

		public function println(str:String):void
		{
			trace(arguments.callee == this.println);
			trace(arguments.length);
			trace(arguments[0]);
			trace(str);
		}

		public function testLogicalComputing()
		{
			var i=5;
			var j=7;
			if (i > j)
			{
				j=9;
				var b:Boolean=true;
			}
			b=(i == 0 || i == 1) && j == 0;
		}

		private function getInt():int
		{
			return counter++;
		}

		public function testInc2()
		{
			var a=[1];
			a[getInt()]++;
			var d=a[getInt()]++;

			var e=++a[getInt()];
			var b=1;
			b++;
			var c=1;
			b=c++;
		}

		protected function testDecl2()
		{
			var i:int=5;
			i+=7;
			if (i == 5)
			{
				if (i < 8)
				{
					var k:int=6;
				}
			}
			k=7;
		}

		public function testChain2()
		{
			var g:Array;
			var h:Boolean;
			var r:int=7;
			var t:int=0;
			t=this.getInt();
			var extraLine:Boolean;
			if (t + 1 < g.length)
			{
				t++;
				h=true;
			}
			if (t >= 0)
			{
				trace("ch");
			}

		}

		public function textXML()
		{
			var name="ahoj";
			var myXML:XML=<order id="604">
					<book isbn="12345">
						<title>{name}</title>
					</book>
				</order>;

			var k=myXML.@id;
			var all=myXML.@*.toXMLString();
			k=myXML.book;
			k=myXML.book.(@isbn="12345");

			var g:XML=new XML("<script>\n\t\t\t\t<![CDATA[\n\t\t\t\t\tfunction() {\n\t\t\t\n\t\t\t\t\t\tFBAS = {\n\t\t\t\n\t\t\t\t\t\t\tsetSWFObjectID: function( swfObjectID ) {\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tFBAS.swfObjectID = swfObjectID;\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tinit: function( opts ) {\n\t\t\t\t\t\t\t\tFB.init( FB.JSON.parse( opts ) );\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tFB.Event.subscribe( \'auth.sessionChange\', function( response ) {\n\t\t\t\t\t\t\t\t\tFBAS.updateSwfSession( response.session );\n\t\t\t\t\t\t\t\t} );\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tsetCanvasAutoResize: function( autoSize, interval ) {\n\t\t\t\t\t\t\t\tFB.Canvas.setAutoResize( autoSize, interval );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tsetCanvasSize: function( width, height ) {\n\t\t\t\t\t\t\t\tFB.Canvas.setSize( { width: width, height: height } );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tlogin: function( opts ) {\n\t\t\t\t\t\t\t\tFB.login( FBAS.handleUserLogin, FB.JSON.parse( opts ) );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\taddEventListener: function( event ) {\n\t\t\t\t\t\t\t\tFB.Event.subscribe( event, function( response ) {\n\t\t\t\t\t\t\t\t\tFBAS.getSwf().handleJsEvent( event, FB.JSON.stringify( response ) );\n\t\t\t\t\t\t\t\t} );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\thandleUserLogin: function( response ) {\n\t\t\t\t\t\t\t\tif( response.session == null ) {\n\t\t\t\t\t\t\t\t\tFBAS.updateSwfSession( null );\n\t\t\t\t\t\t\t\t\treturn;\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tif( response.perms != null ) {\n\t\t\t\t\t\t\t\t\t// user is logged in and granted some permissions.\n\t\t\t\t\t\t\t\t\t// perms is a comma separated list of granted permissions\n\t\t\t\t\t\t\t\t\tFBAS.updateSwfSession( response.session, response.perms );\n\t\t\t\t\t\t\t\t} else {\n\t\t\t\t\t\t\t\t\tFBAS.updateSwfSession( response.session );\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tlogout: function() {\n\t\t\t\t\t\t\t\tFB.logout( FBAS.handleUserLogout );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\thandleUserLogout: function( response ) {\n\t\t\t\t\t\t\t\tswf = FBAS.getSwf();\n\t\t\t\t\t\t\t\tswf.logout();\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tui: function( params ) {\n\t\t\t\t\t\t\t\tobj = FB.JSON.parse( params );\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tmethod = obj.method;\n\t\t\t\t\t\t\t\tcb = function( response ) { FBAS.getSwf().uiResponse( FB.JSON.stringify( response ), method ); }\n\t\t\t\t\t\t\t\tFB.ui( obj, cb );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tgetSession: function() {\n\t\t\t\t\t\t\t\tsession = FB.getSession();\n\t\t\t\t\t\t\t\treturn FB.JSON.stringify( session );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tgetLoginStatus: function() {\n\t\t\t\t\t\t\t\tFB.getLoginStatus( function( response ) {\n\t\t\t\t\t\t\t\t\tif( response.session ) {\n\t\t\t\t\t\t\t\t\t\tFBAS.updateSwfSession( response.session );\n\t\t\t\t\t\t\t\t\t} else {\n\t\t\t\t\t\t\t\t\t\tFBAS.updateSwfSession( null );\n\t\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t\t} );\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tgetSwf: function getSwf() {\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\treturn document.getElementById( FBAS.swfObjectID );\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t},\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tupdateSwfSession: function( session, extendedPermissions ) {\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tswf = FBAS.getSwf();\n\t\t\t\t\t\t\t\textendedPermissions = ( extendedPermissions == null ) ? \'\' : extendedPermissions;\n\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tif( session == null ) {\n\t\t\t\t\t\t\t\t\tswf.sessionChange( null );\n\t\t\t\t\t\t\t\t} else {\n\t\t\t\t\t\t\t\t\tswf.sessionChange( FB.JSON.stringify( session ), FB.JSON.stringify( extendedPermissions.split( \',\' ) ) );\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t};\n\t\t\t\t\t}\n\t\t\t\t]]>\n\t\t\t</script>");
		}

		public function testDoWhile2():int
		{
			var k:int=5;
			do
			{
				k++;
				if (k == 7)
				{
					k=5 * k;
				}
				else
				{
					k=5 - k;
				}
				k--
			} while (k < 9);
			return 2;
		}

		public function testWhileAnd()
		{
			var a:int=5;
			var b:int=10;
			while ((a < 10) && (b > 1))
			{
				a++;
				b--;
			}
			a=7;
			b=9;
		}

		public function testNamedAnonFunctions()
		{
			var test=new function testFunc(param1:*, param2:int, param3:Array):Boolean
				{
					return (param1 as TestClass2).attrib1==5;
				}
		}

		public function testStringConcat()
		{
			var k:int=8;
			traceIt("hello" + 5 * 6);
			traceIt("hello" + (k - 1));
			traceIt("hello" + 5 + 6);
		}

		public function testWhileTry()
		{
			while (true)
			{
				try
				{
					while (true)
					{
						trace("a")
					}
				}
				catch (e:EOFError)
				{
					continue;
				}
				catch (e:Error)
				{
					continue;
				}
			}
		}

		public function testWhileTry2()
		{
			for (var i=0; i < 100; i++)
			{
				try
				{
					for (var j=0; j < 20; j++)
					{
						trace("a")
					}
				}
				catch (e:EOFError)
				{
					continue;
				}
				catch (e:Error)
				{
					continue;
				}
				trace("after_try");
			}
			trace("end");
		}

		public function testTryReturn():int
		{
			try
			{
				var i:int=0;
				var b:Boolean=true;
				if (i > 0)
				{
					while (testDoWhile2())
					{
						if (b)
						{
							return 5;
						}
					}
				}
				i++;
				return 2;
			}
			catch (e:Error)
			{
			}
			finally
			{
			}
			return 4;

		}

		public function testOptionalParameters(p1:Event=null, p2:Number=1, p3:Number=-1, p4:Number=-1.1, p5:Number=-1.1, p6:String="a")
		{
		}
		
		public function testVector2()
		{
			var a:Vector.<Vector.<int>> = new Vector.<Vector.<int>>();
         		var b:Vector.<int> = new <int>[10, 20, 30];
		}
		
		public function testFinallyOnly(){
			var a = 5;
			try{
				a = 9;
				trace("intry");
			}finally {
				trace("infinally");
			}
		}
		
		public function testCatchFinally(){
			var a = 5;
			try{
				a = 9;
				trace("intry");
			}catch(e){
				trace("incatch");
			}finally {
				trace("infinally");
			}
		}
		
		public function other(){
			var n:TestNs = new TestNs();
		}
		
		public function testRegExp() {
			var a1 = new RegExp("[a-z\r\n0-9\\\\]+","i");
			var a2 = /[a-z\r\n0-9\\]+/i;
			
			var b1 = new RegExp("[0-9AB]+");
			var b2 = /[0-9AB]+/;
		}
			
		public function testDefaultNotLast() {
			var k = 10;
			switch(k){
				default:
					trace("def");
				case 5:
					trace("def and 5");
					break;
				case 4:
					trace("4");
					break;					
			}
			trace("after switch");
		}
		
		public function testDefaultNotLastGrouped() {
			var k = 10;
			switch(k){
				default:
				case "six":
					trace("def and 6");
				case "five":
					trace("def and 6 and 5");
					break;
				case "four":
					trace("4");
					break;					
			}
			trace("after switch");
		}
		
		public function testManualConvert(){
			trace("String(this).length");
			trace(String(this).length);
		}
		
		public function testPrecedenceX(){		
			var a = 5;
			var b = 2;
			var c = 3;
			var d = a << (b >>> c);
			var e = a << b >>> c;
		}
	}
}