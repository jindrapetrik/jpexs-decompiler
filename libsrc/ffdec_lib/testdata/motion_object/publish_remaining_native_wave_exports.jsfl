/* Reuse the full roundtrip command, restricted to the missing equivalent case. */
(function () {
    var root = fl.scriptURI.substring(0, fl.scriptURI.lastIndexOf("/") + 1);
    var source = FLfile.read(root + "publish_native_wave_exports.jsfl");
    var marker = "var onlyCase = null;";
    if (!source || source.indexOf(marker) < 0 || source.indexOf(marker) !== source.lastIndexOf(marker)) {
        throw new Error("Cannot load the native wave roundtrip command/filter");
    }
    // This is our local command, not data from an external document.
    eval(source.replace(marker, 'var onlyCase = "bounce_in_s1_601";'));
}());
