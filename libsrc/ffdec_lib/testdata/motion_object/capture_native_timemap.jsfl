/* Capture the selected native motion tween without changing or saving its document. */
(function () {
    var doc = fl.getDocumentDOM();
    if (!doc) { alert("Open an XFL/FLA and select a motion-object span first."); return; }
    var timeline = doc.getTimeline(), layer = timeline.currentLayer, index = timeline.currentFrame;
    var frame = timeline.layers[layer].frames[index];
    if (!frame || !frame.isMotionObject()) {
        alert("Select a motion-object span with an easing applied in the Motion Editor.");
        return;
    }
    var root = fl.scriptURI.substring(0, fl.scriptURI.lastIndexOf("/") + 1);
    var libraryRoot = root.replace(/motion_object\/$/, "").replace(/testdata\/$/, "");
    var output = libraryRoot + "build/motion-diagnostic/native-timemap-capture-" + new Date().getTime() + "/";
    function folder(uri) {
        if (!FLfile.exists(uri) && !FLfile.createFolder(uri)) { throw new Error("Cannot create " + uri); }
    }
    function write(name, value) {
        if (!FLfile.write(output + name, value)) { throw new Error("Cannot write " + name); }
    }
    try {
        folder(libraryRoot + "build/");
        folder(libraryRoot + "build/motion-diagnostic/");
        folder(output);
        write("motion.xml", String(frame.getMotionObjectXML()));
        write("context.txt", "document=" + doc.name + "\r\nlayer=" + layer
                + "\r\nframe=" + index + "\r\nstartFrame=" + frame.startFrame
                + "\r\nduration=" + frame.duration + "\r\nfps=" + doc.frameRate
                + "\r\nwidth=" + doc.width + "\r\nheight=" + doc.height + "\r\n");
        write("STEP-001.txt", "Native motion XML captured. Publishing reference SWF.\r\n");
        doc.exportSWF(output + "reference_native.swf", true);
        if (!FLfile.exists(output + "reference_native.swf")) { throw new Error("Native SWF export failed"); }
        write("STATUS.txt", "Native motion XML and SWF captured.\r\n");
        fl.trace("Native TimeMap capture: " + output);
        alert("Native TimeMap capture completed.\n\n" + output);
    } catch (error) {
        if (FLfile.exists(output)) { FLfile.write(output + "STATUS.txt", "ERROR: " + error); }
        alert("Native TimeMap capture failed: " + error + "\n\n" + output);
    }
}());
