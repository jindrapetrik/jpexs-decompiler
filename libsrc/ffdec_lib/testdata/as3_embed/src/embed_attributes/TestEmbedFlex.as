package embed_attributes
{ 
    import flash.display.Sprite; 
    import flash.media.Sound;
    import flash.display.Bitmap;
    import flash.display.MovieClip;
    import flash.media.Sound;
    import flash.utils.ByteArray;
 
    public class TestEmbedFlex extends Sprite 
    { 
        [Embed(source="../../assets/image.png")] 
        public var imagePngClass:Class;
        
        [Embed(source="../../assets/image.png")] 
        public var sameImageClass:Class; 
        
        [Embed(source="../../assets/image_grid.png", scaleGridTop="10", scaleGridBottom="40", 
                scaleGridLeft="10", scaleGridRight="70")] 
        public var imageGridPngClass:Class;
                
        //deprecated in Flex, not supported in AIR
        [Embed(source="../../assets/image.svg")]
        public var imageSvgClass:Class;
        
        [Embed(source="../../assets/movie.swf")]
        public var movieSwfClass:Class;
        
        [Embed(source="../../assets/movie_singleframe.swf")]
        public var movieSingleFrameSwfClass:Class;
        
        [Embed(source="../../assets/sound.mp3")] 
        public var soundClass:Class; 
        
        [Embed(
          source="../../assets/font.ttf",
          fontFamily="Great Vibes",
          fontWeight="normal",
          fontStyle="normal",
          mimeType="application/x-font-truetype",
          unicodeRange="U+0020,U+0041-005A", 
      	  advancedAntiAliasing="true",
          embedAsCFF="false"
          )]
        public var fontClass:Class;
                     
        [Embed(source="../../assets/data.bin", mimeType="application/octet-stream")] 
        public var binaryDataClass:Class;                 
        
        [Embed(source="../../assets/movie_symbol.swf", symbol="symbols.MySymbol")]
        public var swfSymbolClass:Class; 
        
        
         
        public function TestEmbedFlex()
        { 
            var someBitmap:Bitmap = new imagePngClass() as Bitmap;
            trace("Dimensions: " + someBitmap.width + "x" + someBitmap.height);
            
            var someGridSprite:Sprite = new imageGridPngClass() as Sprite;            
            
            var someSprite:Sprite = new imageSvgClass() as Sprite;
            
            var someMovieClip:MovieClip = new movieSwfClass() as MovieClip;
            
            var someSpriteSwf:Sprite = new movieSingleFrameSwfClass() as Sprite;
            
            var someSound:Sound = new soundClass() as Sound; 
            someSound.play(); 
            
            var someData:ByteArray = new binaryDataClass() as ByteArray;
            trace("Length: " + someData.length); 
            
            var symbol:Sprite = new swfSymbolClass() as Sprite;
        } 
    } 
}
