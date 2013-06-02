package classes
{
	import flash.utils.ByteArray;
	import flash.utils.Dictionary;
	import com.hurlant.math.BigInteger;
	import flash.filters.ColorMatrixFilter;
	import com.wirelust.as3zlib.Inflate;

	public class TestClass3
	{
		/*[Embed(source="image.png",
		   mimeType="image/png")]
		private static var pngImg:Class; 
		
		[Embed(source="image.jpg",
		   mimeType="image/jpeg")]
		private static var jpegImg:Class; 
		
		[Embed(source="image.gif",
		   mimeType="image/gif")]
		private static var gifImg:Class; 
		
		[Embed(source = "malgun.ttf", fontName="Malgun", mimeType = 'application/x-font', embedAsCFF = "false")]
		public static const fnt:Class;
		
		[Embed(source = "malgun.ttf", fontName="Malgun", mimeType = 'application/x-font-truetype', embedAsCFF = "false")]
		public static const fntttf:Class;
		
		[Embed(source="as2.swf",
		   mimeType="application/x-shockwave-flash")]
		private static var swf:Class;
		
		[Embed(source="audio.mp3",
		   mimeType="audio/mpeg")]
		private static var mpg:Class;
		
		[Embed(source="image.svg",
		   mimeType="image/svg")]
		private static var svg:Class;
		
		[Embed(source="image.svg",
		   mimeType="image/svg-xml")]
		private static var svgxml:Class;
		
		[Embed(source="text.txt",
		   mimeType="application/octet-stream")]
		private static var txt:Class;*/
		
		    /*var c = [1, 2, 3, 4, 5];
			var b = 1;
			trace("++arr");
			b = ++c[2];
			trace("arr++");
			b = c[2]++;
			trace("--arr");
			b = --c[2];
			trace("arr--");
			b = c[2]--;*/
		
		
		public function testIncDec()
		{
			var a = 5;
			var b = 0;
			trace("++var");
			b = ++a;
			trace("var++");
			b = a++;
			trace("--var");
			b = --a;
			trace("var--");
			b = a--;			
		}
	}
}
