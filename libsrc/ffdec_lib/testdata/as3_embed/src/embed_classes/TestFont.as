package embed_classes
{ 
    import flash.text.Font;
    
    //not supported in Flex, AIR only
    [Embed(source="../../assets/font.ttf", fontFamily="Great Vibes", fontWeight= "normal", fontStyle = "normal", mimeType="application/x-font-truetype")]
    public class TestFont extends Font
    {
    } 
}
