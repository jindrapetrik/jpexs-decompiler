/* Capture native gradient property encoding and author keys using that encoding. */
(function () {
    var root = fl.scriptURI.substring(0, fl.scriptURI.lastIndexOf("/") + 1);
    var libraryRoot = root.replace(/motion_object\/$/, "").replace(/testdata\/$/, "");
    var output = libraryRoot + "build/motion-diagnostic/native-gradient-filters-" + new Date().getTime() + "/";
    var active = fl.getDocumentDOM();
    var cases = ["25_gradient_glow", "26_gradient_bevel", "27_gradient_colors", "28_gradient_ratios", "29_gradient_topology", "30_gradient_stack"];
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
    function select(doc, frame, requireMotion) {
        var timeline = doc.getTimeline();
        timeline.currentLayer = 0;
        timeline.currentFrame = frame;
        timeline.setSelectedFrames(frame, frame + 1, true);
        if (requireMotion !== false && !timeline.layers[0].frames[frame].isMotionObject()) { throw new Error("Motion object lost at " + frame); }
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
            for (var field in filters[i]) {
                try { lines.push(field + " (" + typeof filters[i][field] + ")=" + String(filters[i][field])); }
                catch (error) { lines.push(field + "=<unreadable>"); }
            }
        }
        return lines.join("\r\n") + "\r\n";
    }
    function frameFilters(xml, target) {
        var frames = xml.match(/<DOMFrame\b[^>]*>[\s\S]*?<\/DOMFrame>/g), result = null;
        if (!frames) { throw new Error("No source DOM frames"); }
        for (var i = 0; i < frames.length; i++) {
            var index = /\bindex="([0-9]+)"/.exec(frames[i]);
            if (!index || Number(index[1]) > target) { continue; }
            var filters = /<filters>[\s\S]*?<\/filters>/.exec(frames[i]);
            if (filters) { result = filters[0]; }
        }
        if (!result) { throw new Error("No source gradient filters at " + target); }
        return result;
    }
    function replaceFilters(xml, filters) {
        // An existing motion object's empty Filters container overrides its
        // static symbol filters on native import. Import a static instance and
        // let CS6 construct the motion object from that filtered instance.
        xml = xml.replace(/<motionObjectXML>[\s\S]*?<\/motionObjectXML>/g, "")
            .replace(/\s+(?:tweenType|isMotionObject|visibleAnimationKeyframes|motionTweenRotate|motionTweenScale|animationType)="[^"]*"/g, "")
            .replace(/\bkeyMode="8195"/g, 'keyMode="9728"');
        var instance = /<DOMSymbolInstance\b[^>]*>[\s\S]*?<\/DOMSymbolInstance>/.exec(xml);
        if (!instance) { throw new Error("No base symbol instance"); }
        var replacement = instance[0].replace(/<filters>[\s\S]*?<\/filters>/, "");
        replacement = replacement.replace("</DOMSymbolInstance>", filters + "</DOMSymbolInstance>");
        return xml.substring(0, instance.index) + replacement + xml.substring(instance.index + instance[0].length);
    }
    function properties(xml) {
        var section = filterSection(xml), chunks = section.match(/<Property\b[^>]*\/>|<Property\b[^>]*>[\s\S]*?<\/Property>/g), result = {};
        if (!chunks) { throw new Error("Native gradient filter properties missing"); }
        for (var i = 0; i < chunks.length; i++) {
            var id = /\bid="([^"]+)"/.exec(chunks[i]);
            if (!id || result[id[1]]) { throw new Error("Missing or duplicate native property id"); }
            result[id[1]] = chunks[i];
        }
        return result;
    }
    function keyedMotion(poses) {
        var maps = [properties(poses[0]), properties(poses[1]), properties(poses[2])], xml = poses[0], count = 0;
        for (var id in maps[0]) {
            var chunks = [maps[0][id], maps[1][id], maps[2][id]];
            if (!chunks[1] || !chunks[2]) { throw new Error("Native topology differs for " + id); }
            if (chunks[0] === chunks[1] && chunks[1] === chunks[2]) { continue; }
            var keys = [];
            for (var p = 0; p < 3; p++) {
                var matches = chunks[p].match(/<Keyframe\b[^>]*\/>|<Keyframe\b[^>]*>[\s\S]*?<\/Keyframe>/g);
                if (!matches || matches.length !== 1) { throw new Error("Native property has no single pose key: " + id); }
                keys.push(matches[0].replace(/\btimevalue="[^"]*"/, 'timevalue="' + p * 30000 + '"'));
            }
            var property = chunks[0].replace(/<Keyframe\b[^>]*\/>|<Keyframe\b[^>]*>[\s\S]*?<\/Keyframe>/g, "");
            property = property.replace(/\s+ignoreTimeMap="[^"]*"/, "");
            property = property.replace(/<Property\b/, '<Property ignoreTimeMap="1"');
            property = property.replace("</Property>", keys.join("") + "</Property>");
            xml = xml.replace(chunks[0], property); count++;
        }
        if (!count) { throw new Error("No changed native gradient properties"); }
        return xml;
    }
    folder(libraryRoot + "build/");
    folder(libraryRoot + "build/motion-diagnostic/");
    if (FLfile.exists(output)) { throw new Error("Output already exists"); }
    folder(output);
    for (var c = 0; c < cases.length; c++) {
        var id = cases[c], doc = null;
        try {
            var destination = output + id + "/", poses = [];
            folder(destination);
            var source = FLfile.read(root + id + "/DOMDocument.xml");
            if (!source) { throw new Error("Cannot read gradient fixture"); }
            for (var pose = 0; pose < 3; pose++) {
                var input = destination + "pose_" + pose + "/";
                copy(root + "01_position/", input);
                var base = FLfile.read(input + "DOMDocument.xml");
                if (!base) { throw new Error("Cannot read base document"); }
                var filters = frameFilters(source, pose * 30);
                write(input + "DOMDocument.xml", replaceFilters(base, filters));
                write(input + "source_filters.xml", filters);
                doc = fl.openDocument(input + "01_position.xfl");
                if (!doc) { throw new Error("Cannot open native pose"); }
                fl.setActiveWindow(doc); select(doc, 0, false);
                write(input + "before_conversion_filters.txt", dumpFilters(doc));
                doc.getTimeline().createMotionObject(0, 61);
                select(doc, 0);
                // Retain native diagnostic data even when import validation
                // fails, so the next diagnosis does not lose the schema.
                write(input + "native_motion.xml", motion(doc));
                write(input + "native_filters.txt", dumpFilters(doc));
                var names = doc.getFilters(), expected = (filters.match(/<Gradient(?:Glow|Bevel)Filter\b/g) || []).length;
                if (names.length !== expected || expected === 0) { throw new Error("Native gradient count differs"); }
                for (var n = 0; n < names.length; n++) {
                    if (names[n].name !== "gradientGlowFilter" && names[n].name !== "gradientBevelFilter") { throw new Error("Native gradient type differs"); }
                }
                poses.push(motion(doc));
                write(input + "native_motion.xml", poses[pose]);
                write(input + "native_filters.txt", dumpFilters(doc));
                var poseFla = input + "native_pose.fla";
                if (!fl.saveDocument(doc, poseFla) || !FLfile.exists(poseFla)) { throw new Error("Native pose save failed"); }
                fl.closeDocument(doc, false); doc = null;
            }
            doc = fl.openDocument(destination + "pose_0/native_pose.fla");
            if (!doc) { throw new Error("Cannot reopen native base pose"); }
            select(doc, 0);
            var note;
            if (id === "29_gradient_topology") {
                note = "topology pose encodings captured; boundary retained in source fixture";
            } else {
                var authored = keyedMotion(poses);
                write(destination + "requested_motion.xml", authored);
                doc.getTimeline().layers[0].frames[0].setMotionObjectXML(authored, false);
                write(destination + "authored_motion.xml", motion(doc));
                note = "native gradient keys authored; SWF fidelity needs comparison";
            }
            var fla = destination + "native_gradient.fla";
            if (!fl.saveDocument(doc, fla) || !FLfile.exists(fla)) { throw new Error("Native animation save failed"); }
            fl.closeDocument(doc, false); doc = null;
            doc = fl.openDocument(fla);
            if (!doc) { throw new Error("Cannot reopen native gradient animation"); }
            write(destination + "persisted_motion.xml", motion(doc));
            doc.exportSWF(destination + "reference_native.swf", true);
            if (!FLfile.exists(destination + "reference_native.swf")) { throw new Error("SWF export failed"); }
            results.push(id + ": " + note);
        } catch (error) { results.push(id + ": FAILED: " + String(error)); }
        finally { if (doc) { fl.closeDocument(doc, false); } }
        write(output + "STATUS.txt", results.join("\r\n") + "\r\n");
    }
    if (active) { fl.setActiveWindow(active); }
    fl.trace("Native gradient capture: " + output);
    alert("Native gradient capture finished.\n" + results.join("\n") + "\n\n" + output);
}());
