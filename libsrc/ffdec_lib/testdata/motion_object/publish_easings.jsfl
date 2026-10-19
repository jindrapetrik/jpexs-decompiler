/* Compile the additional easing examples without changing their reference SWFs. */
(function () {
    var root = fl.scriptURI.substring(0, fl.scriptURI.lastIndexOf("/") + 1);
    var active = fl.getDocumentDOM(), owned = null;
    var libraryRoot = root.replace(/motion_object\/$/, "").replace(/testdata\/$/, "");
    var scratch = libraryRoot + "build/motion-diagnostic/easing-publish-" + new Date().getTime() + "/";
    var cases = ["37_ease_quad", "38_ease_cubic", "39_ease_quart", "40_ease_quint",
        "41_ease_sine", "42_ease_circ", "43_ease_expo", "44_ease_back",
        "45_ease_bounce", "46_ease_elastic"];
    var report = [], failed = false;
    function copy(source, destination) {
        if (!FLfile.createFolder(destination)) { throw new Error("Cannot create " + destination); }
        var files = FLfile.listFolder(source, "files"), dirs = FLfile.listFolder(source, "directories");
        if (!files || !dirs) { throw new Error("Cannot list " + source); }
        for (var f = 0; f < files.length; f++) {
            if (!FLfile.copy(source + files[f], destination + files[f])) {
                throw new Error("Cannot copy " + source + files[f]);
            }
        }
        for (var d = 0; d < dirs.length; d++) { copy(source + dirs[d] + "/", destination + dirs[d] + "/"); }
    }
    try {
        if (!FLfile.exists(libraryRoot + "build/")) { FLfile.createFolder(libraryRoot + "build/"); }
        if (!FLfile.exists(libraryRoot + "build/motion-diagnostic/")) { FLfile.createFolder(libraryRoot + "build/motion-diagnostic/"); }
        if (!FLfile.createFolder(scratch)) { throw new Error("Cannot create " + scratch); }
        for (var i = 0; i < cases.length; i++) {
            var id = cases[i], folder = root + id + "/";
            fl.trace("Compiling " + id);
            FLfile.write(scratch + "STATUS.txt", report.join("\r\n") + "\r\nRUNNING: " + id);
            copy(folder, scratch + id + "/");
            owned = fl.openDocument(scratch + id + "/" + id + ".xfl");
            if (!owned) { throw new Error("Cannot open " + id); }
            if (!owned.getTimeline().layers[0].frames[0].isMotionObject()) {
                throw new Error("Missing motion object in " + id);
            }
            var output = folder + id + "_compiled.swf";
            owned.exportSWF(output, true);
            if (!FLfile.exists(output)) { throw new Error("Missing output for " + id); }
            report.push("Compiled " + id);
            FLfile.write(scratch + "STATUS.txt", report.join("\r\n"));
            fl.closeDocument(owned, false);
            owned = null;
        }
    } catch (error) {
        failed = true;
        report.push("ERROR: " + error);
        FLfile.write(scratch + "STATUS.txt", report.join("\r\n"));
    } finally {
        if (owned) { fl.closeDocument(owned, false); }
        if (active) { fl.setActiveWindow(active); }
        fl.trace(report.join("\n"));
        if (failed) { alert(report.join("\n")); }
    }
}());
