/* Publish the filter fixtures, including the precision fallback, in fresh copies. */
(function () {
    var root = fl.scriptURI.substring(0, fl.scriptURI.lastIndexOf("/") + 1);
    var libraryRoot = root.replace(/motion_object\/$/, "").replace(/testdata\/$/, "");
    var output = libraryRoot + "build/motion-diagnostic/native-filter-roundtrip-" + new Date().getTime() + "/";
    var ids = ["18_blur", "19_glow", "20_drop_shadow", "21_bevel", "22_filter_stack", "23_filter_parameters", "24_filter_integer_strength"];
    var active = fl.getDocumentDOM(), results = [];
    function folder(uri) {
        if (!FLfile.exists(uri) && !FLfile.createFolder(uri)) { throw new Error("Cannot create " + uri); }
    }
    function write(uri, value) {
        if (!FLfile.write(uri, value)) { throw new Error("Cannot write " + uri); }
    }
    function copy(source, destination) {
        folder(destination);
        var files = FLfile.listFolder(source, "files"), dirs = FLfile.listFolder(source, "directories"), i;
        if (!files || !dirs) { throw new Error("Cannot list " + source); }
        for (i = 0; i < files.length; i++) {
            if (!FLfile.copy(source + files[i], destination + files[i])) { throw new Error("Cannot copy " + files[i]); }
        }
        for (i = 0; i < dirs.length; i++) { copy(source + dirs[i] + "/", destination + dirs[i] + "/"); }
    }
    folder(libraryRoot + "build/");
    folder(libraryRoot + "build/motion-diagnostic/");
    if (FLfile.exists(output)) { throw new Error("Diagnostic output exists"); }
    folder(output);
    for (var i = 0; i < ids.length; i++) {
        var id = ids[i], doc = null;
        try {
            var destination = output + id + "/";
            copy(root + id + "/", destination);
            doc = fl.openDocument(destination + id + ".xfl");
            if (!doc) { throw new Error("Cannot open diagnostic copy"); }
            var timeline = doc.getTimeline();
            var motion = timeline.layers[0].frames[0].isMotionObject();
            if (timeline.layers[0].frames.length !== 61 || motion !== (id !== "23_filter_parameters")) {
                throw new Error("Native import lost the motion object or changed frame count");
            }
            if (motion) { write(destination + "imported_motion.xml", String(timeline.layers[0].frames[0].getMotionObjectXML())); }
            var fla = destination + "native_export.fla";
            if (!fl.saveDocument(doc, fla) || !FLfile.exists(fla)) { throw new Error("Native FLA save failed"); }
            doc.exportSWF(destination + "published.swf", true);
            if (!FLfile.exists(destination + "published.swf")) { throw new Error("Native SWF export failed"); }
            results.push(id + ": published; reference comparison pending");
        } catch (error) { results.push(id + ": FAILED: " + String(error)); }
        finally { if (doc) { fl.closeDocument(doc, false); } }
        write(output + "STATUS.txt", results.join("\r\n") + "\r\n");
    }
    if (active) { fl.setActiveWindow(active); }
    fl.trace("Native filter roundtrip: " + output);
    alert("Native filter roundtrip finished.\n" + results.join("\n") + "\n\n" + output);
}());
