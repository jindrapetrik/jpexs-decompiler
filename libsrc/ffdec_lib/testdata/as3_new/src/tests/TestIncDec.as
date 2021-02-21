package tests
{
	
	public class TestIncDec
	{
		
		private var attrx:int = 0;
		
		public function run():*
		{
			var a:* = 5;
			var b:* = 0;
			trace("++var");
			b = ++a;
			trace("var++");
			b = a++;
			trace("--var");
			b = --a;
			trace("var--");
			b = a--;
			var c:* = [1, 2, 3, 4, 5];
			trace("++arr");
			b = ++c[2];
			trace("arr++");
			b = c[2]++;
			trace("--arr");
			b = --c[2];
			trace("arr--");
			b = c[2]--;
			var d:* = new TestClass1();
			trace("++property");
			trace(++d.attrib);
			trace("property++");
			trace(d.attrib++);
			trace("--property");
			trace(--d.attrib);
			trace("property--");
			trace(d.attrib--);
			trace("arr[e++]");
			var chars:Array = new Array(36);
			var index:uint = 0;
			chars[index++] = 5;
			trace("arr[++e]");
			chars[++index] = 5;
			trace("attr++");			
			trace(attrx++);
			attrx++;
			trace("attr--");
			trace(attrx--);
			attrx--;
			trace("++attr");
			trace(++attrx);
			++attrx;
			trace("--attr");
			trace(--attrx);
			--attrx;
			
		}
	}
}

class TestClass1
{
	public var attrib:int = 5;
}