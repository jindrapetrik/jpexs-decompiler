package components {
    import flash.utils.ByteArray;

    public class AssetHolder {
        [Embed(source="../../../as3_embed/assets/data.bin", mimeType="application/octet-stream")]
        private var payload:Class;

        [Embed(source="../../../as3_embed/assets/sound.mp3")]
        public var sound:Class;

        [Embed(source="../../../as3_embed/assets/font.ttf", fontFamily="ShowcaseFont", embedAsCFF="false",
            unicodeRange="U+0020,U+0041-005A")]
        public var classicFont:Class;

        public function get description():String {
            var bytes:ByteArray = new payload() as ByteArray;
            return "Embedded binary payload: " + bytes.length + " bytes";
        }
    }
}
