package embed_classes
{ 
    import flash.text.Font;
    
    [Embed(
    source="../../assets/font.ttf",
    fontFamily="Great Vibes",
    fontWeight="normal",
    fontStyle="normal",
    mimeType="application/x-font-truetype",
    unicodeRange="U+0030-0039,U+002E", 
	advancedAntiAliasing="true",
    embedAsCFF="true"
    )]
    public class TestFontCFF extends Font
    {
    } 
}
