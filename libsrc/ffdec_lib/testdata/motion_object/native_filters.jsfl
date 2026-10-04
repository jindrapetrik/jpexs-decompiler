/* Obtain native CS6 filter motion XML without guessing property identifiers. */
(function () {
    var root = fl.scriptURI.substring(0, fl.scriptURI.lastIndexOf("/") + 1);
    var libraryRoot = root.replace(/motion_object\/$/, "").replace(/testdata\/$/, "");
    var output = libraryRoot + "build/motion-diagnostic/native-filters-" + new Date().getTime() + "/";
    var active = fl.getDocumentDOM();
    var cases = [
        {id: "18_blur", filters: ["blurFilter"]},
        {id: "19_glow", filters: ["glowFilter"]},
        {id: "20_drop_shadow", filters: ["dropShadowFilter"]},
        {id: "21_bevel", filters: ["bevelFilter"]},
        {id: "22_filter_stack", filters: ["blurFilter", "glowFilter", "dropShadowFilter"]}
    ];
    var results = [];
    function folder(uri) {
        if (!FLfile.exists(uri) && !FLfile.createFolder(uri)) { throw new Error("Cannot create " + uri); }
    }
    function write(uri, text) {
        if (!FLfile.write(uri, text)) { throw new Error("Cannot write " + uri); }
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
    function select(doc, frame) {
        var timeline = doc.getTimeline();
        timeline.currentLayer = 0;
        timeline.currentFrame = frame;
        timeline.setSelectedFrames(frame, frame + 1, true);
        if (!timeline.layers[0].frames[frame].isMotionObject()) { throw new Error("Motion object lost at " + frame); }
        doc.selectAll();
        if (doc.selection.length !== 1 || doc.selection[0].elementType !== "instance") {
            throw new Error("Expected one selected symbol instance");
        }
    }
    function motion(doc) {
        var frame = doc.getTimeline().layers[0].frames[0];
        if (!frame.isMotionObject()) { throw new Error("Motion object lost"); }
        return String(frame.getMotionObjectXML());
    }
    function filterSection(xml) {
        var start = /<PropertyContainer\b[^>]*\bid="Filters"[^>]*>/.exec(xml);
        if (!start) { return ""; }
        var tail = xml.substring(start.index), depth = 0, position = 0;
        // Avoid nested RegExp execution while iterating: the CS6 JSFL runtime
        // failed that scan for the native packed-color properties.
        while (position < tail.length) {
            var open = tail.indexOf("<", position), close = tail.indexOf(">", open);
            if (open < 0 || close < 0) { break; }
            var tag = tail.substring(open, close + 1);
            if (tag.indexOf("</PropertyContainer") === 0) { depth--; }
            else if (tag.indexOf("<PropertyContainer") === 0 && tag.charAt(tag.length - 2) !== "/") { depth++; }
            position = close + 1;
            if (depth === 0) { return tail.substring(0, position); }
        }
        throw new Error("Unbalanced filter motion XML");
    }
    function dumpFilters(doc) {
        var filters = doc.getFilters(), lines = [];
        for (var i = 0; i < filters.length; i++) {
            lines.push("filter " + i + ": " + filters[i].name);
            var fields = ["enabled", "blurX", "blurY", "quality", "color", "strength", "angle",
                "distance", "inner", "knockout", "hideObject", "type", "shadowColor", "highlightColor"];
            for (var p = 0; p < fields.length; p++) {
                var value = filters[i][fields[p]];
                if (typeof value !== "undefined") { lines.push(fields[p] + "=" + String(value)); }
            }
        }
        return lines.join("\r\n") + "\r\n";
    }
    function setPose(doc, names, pose) {
        // Midpoint and endpoint values deliberately differ by channel. Use
        // document setters to request native authoring of property keys.
        var filters = doc.getFilters();
        for (var i = 0; i < names.length; i++) {
            filters[i].blurX = [2, 18, 28][pose];
            filters[i].blurY = [4, 6, 16][pose];
            if (names[i] !== "blurFilter") {
                filters[i].strength = [50, 120, 180][pose];
                if (names[i] === "bevelFilter") {
                    filters[i].shadowColor = ["#112244", "#442211", "#2244FF"][pose];
                    filters[i].highlightColor = ["#FFFFFF", "#FFCC66", "#FF8822"][pose];
                } else {
                    filters[i].color = ["#2244FF", "#66CC88", "#FF8822"][pose];
                }
            }
            if (names[i] === "dropShadowFilter" || names[i] === "bevelFilter") {
                filters[i].angle = [45, 75, 135][pose];
                filters[i].distance = [3, 12, 25][pose];
            }
        }
        // Apply the full pose once. Sequential native setters restored earlier
        // channels to their base values in the first capture.
        doc.setFilters(filters);
    }
    folder(libraryRoot + "build/");
    folder(libraryRoot + "build/motion-diagnostic/");
    if (FLfile.exists(output)) { throw new Error("Output already exists"); }
    folder(output);
    for (var c = 0; c < cases.length; c++) {
        var spec = cases[c], doc = null;
        try {
            var destination = output + spec.id + "/";
            folder(destination);
            copy(root + "01_position/", destination + "input/");
            doc = fl.openDocument(destination + "input/01_position.xfl");
            if (!doc) { throw new Error("Cannot open diagnostic copy"); }
            fl.setActiveWindow(doc);
            if (doc.getTimeline().layers[0].frames.length !== 61) { throw new Error("Unexpected frame count"); }
            select(doc, 0);
            for (var i = 0; i < spec.filters.length; i++) { doc.addFilter(spec.filters[i]); }
            if (doc.getFilters().length !== spec.filters.length) { throw new Error("Native filter count differs"); }
            setPose(doc, spec.filters, 0);
            var initial = motion(doc);
            write(destination + "initial_motion.xml", initial);
            write(destination + "initial_filters.txt", dumpFilters(doc));
            select(doc, 30);
            setPose(doc, spec.filters, 1);
            write(destination + "mid_filters.txt", dumpFilters(doc));
            select(doc, 60);
            setPose(doc, spec.filters, 2);
            write(destination + "end_filters.txt", dumpFilters(doc));
            var finalMotion = motion(doc);
            write(destination + "authored_motion.xml", finalMotion);
            var fla = destination + spec.id + ".fla";
            if (!fl.saveDocument(doc, fla) || !FLfile.exists(fla)) { throw new Error("Native FLA save failed"); }
            fl.closeDocument(doc, false);
            doc = null;
            doc = fl.openDocument(fla);
            if (!doc) { throw new Error("Cannot reopen native FLA"); }
            var persisted = motion(doc);
            write(destination + "persisted_motion.xml", persisted);
            doc.exportSWF(destination + "reference_native.swf", true);
            if (!FLfile.exists(destination + "reference_native.swf")) { throw new Error("SWF export failed"); }
            var section = filterSection(persisted);
            var animated = /<Keyframe\b[^>]*\btimevalue="[1-9][0-9]*"/.test(section);
            results.push(spec.id + ": " + (animated ? "native filter keys captured; SWF fidelity still needs comparison" :
                "NO ANIMATED FILTER KEYS; retained FLA, XML and SWF for diagnosis"));
        } catch (error) { results.push(spec.id + ": FAILED: " + String(error)); }
        finally { if (doc) { fl.closeDocument(doc, false); } }
        write(output + "STATUS.txt", results.join("\r\n") + "\r\n");
    }
    if (active) { fl.setActiveWindow(active); }
    fl.trace("Native filter capture: " + output);
    alert("Native filter capture finished.\n" + results.join("\n") + "\n\n" + output);
}());
