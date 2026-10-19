/* Publish staged skew fixes and unchanged controls without editing source XFLs. */
(function () {
    var root = fl.scriptURI.substring(0, fl.scriptURI.lastIndexOf("/") + 1);
    var library = root.replace(/testdata\/motion_object\/$/, "");
    var staged = library + "build/motion-diagnostic/skew-fix/";
    var output = library + "build/motion-diagnostic/native-skew-fix-" + new Date().getTime() + "/";
    var cases = [
        [staged, "05_animated_skew", false],
        [staged, "16_rotation_skew_dual_quadratic", false],
        [staged, "quarter_degree", true],
        [root, "03_rotation", true],
        [root, "04_static_skew", true]
    ];
    var active = fl.getDocumentDOM(), results = [];
    function folder(uri) {
        if (!FLfile.exists(uri) && !FLfile.createFolder(uri)) { throw new Error("Cannot create " + uri); }
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
    if (!FLfile.exists(staged)) { throw new Error("Missing staged skew-fix XFLs: " + staged); }
    if (FLfile.exists(output)) { throw new Error("Output already exists"); }
    folder(output);
    for (var i = 0; i < cases.length; i++) {
        var source = cases[i][0], id = cases[i][1], expectedMotion = cases[i][2], doc = null;
        try {
            var destination = output + id + "/";
            copy(source + id + "/", destination);
            doc = fl.openDocument(destination + id + ".xfl");
            if (!doc) { throw new Error("Cannot open XFL"); }
            var timeline = doc.getTimeline(), motion = 0;
            for (var l = 0; l < timeline.layers.length; l++) {
                var frames = timeline.layers[l].frames;
                for (var f = 0; f < frames.length; f++) {
                    if (frames[f].startFrame === f && frames[f].isMotionObject()) { motion++; }
                }
            }
            if ((motion > 0) !== expectedMotion) { throw new Error("Unexpected motion-object count: " + motion); }
            doc.exportSWF(destination + "published.swf", true);
            if (!FLfile.exists(destination + "published.swf")) { throw new Error("SWF export failed"); }
            results.push(id + ": published; motion objects=" + motion);
        } catch (error) { results.push(id + ": FAILED: " + String(error)); }
        finally { if (doc) { fl.closeDocument(doc, false); } }
        if (!FLfile.write(output + "STATUS.txt", results.join("\r\n") + "\r\n")) { throw new Error("Cannot write status"); }
    }
    if (active) { fl.setActiveWindow(active); }
    fl.trace("Skew fix verification: " + output);
    alert("Skew fix publishing finished.\n" + results.join("\n") + "\n\n" + output);
}());
