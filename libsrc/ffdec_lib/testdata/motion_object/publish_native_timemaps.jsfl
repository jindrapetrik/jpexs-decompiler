/* Publish and capture the native Quadratic/DualQuadratic regression examples. */
(function () {
    var root = fl.scriptURI.substring(0, fl.scriptURI.lastIndexOf("/") + 1);
    var active = fl.getDocumentDOM(), owned = null;
    var libraryRoot = root.replace(/motion_object\/$/, "").replace(/testdata\/$/, "");
    var scratch = libraryRoot + "build/motion-diagnostic/native-timemap-publish-" + new Date().getTime() + "/";
    var cases = ["47_native_quadratic", "48_native_mixed_maps"];
    var report = [], warnings = [], checkpointNumber = 0;
    function diagnostic(name, value) {
        var written = false;
        try { written = FLfile.write(scratch + name, value); } catch (error) {}
        if (!written) {
            var message = "Cannot write diagnostic " + scratch + name;
            warnings.push(message);
            fl.trace("WARNING: " + message);
        }
    }
    function capture(name, value) {
        if (!FLfile.write(scratch + name, value)) {
            throw new Error("Cannot write native motion capture " + scratch + name);
        }
    }
    function checkpoint(step) {
        checkpointNumber++;
        // Immutable files avoid repeatedly overwriting a file on a shared or
        // synchronized drive. Diagnostic failures must not stop publication.
        diagnostic("STEP-" + ("000" + checkpointNumber).slice(-3) + ".txt", step + "\r\n");
        fl.trace(step);
    }
    function attribute(tag, name) {
        var match = new RegExp("\\b" + name + '="([^"]*)"').exec(tag);
        return match ? match[1] : null;
    }
    function validateMaps(xml, id) {
        var maps = xml.match(/<TimeMap\b[^>]*>/g) || [];
        var properties = xml.match(/<Property\b[^>]*>/g) || [];
        var expected = id === "47_native_quadratic"
                ? {Motion_X: ["Quadratic", -40], Motion_Y: ["Quadratic", 65], Scale_X: ["Quadratic", -40]}
                : {Motion_X: ["Quadratic", 50], Motion_Y: ["DualQuadratic", 50], Scale_X: ["Quadratic", 50]};
        for (var property in expected) {
            var found = false;
            for (var p = 0; p < properties.length; p++) {
                var tag = properties[p];
                if (attribute(tag, "id") !== property) { continue; }
                var index = Number(attribute(tag, "TimeMapIndex") || 0), map = maps[index];
                if (attribute(tag, "ignoreTimeMap") !== "0" || !map
                        || attribute(map, "type") !== expected[property][0]
                        || Number(attribute(map, "strength")) !== expected[property][1]) {
                    throw new Error("Native map assignment lost for " + property + " in " + id);
                }
                found = true;
                break;
            }
            if (!found) { throw new Error("Missing " + property + " in " + id); }
        }
    }
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
            checkpoint(id + ": preparing diagnostic copy");
            copy(folder, scratch + id + "/");
            checkpoint(id + ": opening exported XFL");
            owned = fl.openDocument(scratch + id + "/" + id + ".xfl");
            if (!owned) { throw new Error("Cannot open " + id); }
            if (!owned.getTimeline().layers[0].frames[0].isMotionObject()) {
                throw new Error("Missing motion object in " + id);
            }
            var xml = String(owned.getTimeline().layers[0].frames[0].getMotionObjectXML());
            capture(id + "/imported_motion.xml", xml);
            validateMaps(xml, id);
            var nativeFla = scratch + id + "/native_export.fla";
            checkpoint(id + ": saving native FLA");
            if (!fl.saveDocument(owned, nativeFla)) { throw new Error("Cannot save " + id); }
            fl.closeDocument(owned, false);
            owned = null;
            checkpoint(id + ": reopening native FLA");
            owned = fl.openDocument(nativeFla);
            if (!owned || !owned.getTimeline().layers[0].frames[0].isMotionObject()) {
                throw new Error("Motion object lost after reopening " + id);
            }
            xml = String(owned.getTimeline().layers[0].frames[0].getMotionObjectXML());
            capture(id + "/persisted_motion.xml", xml);
            validateMaps(xml, id);
            var output = folder + id + "_compiled.swf";
            checkpoint(id + ": publishing SWF");
            owned.exportSWF(output, true);
            if (!FLfile.exists(output)) { throw new Error("Missing output for " + id); }
            report.push("Compiled " + id);
            diagnostic(id + "/RESULT.txt", "Compiled " + id + "\r\n");
            fl.closeDocument(owned, false);
            owned = null;
        }
    } catch (error) {
        report.push("ERROR: " + error);
    } finally {
        if (owned) { fl.closeDocument(owned, false); }
        if (active) { fl.setActiveWindow(active); }
        diagnostic("STATUS.txt", report.join("\r\n") + "\r\n");
        if (warnings.length) { report.push("Diagnostic warnings:\n" + warnings.join("\n")); }
        fl.trace(report.join("\n"));
        fl.trace("Native TimeMap diagnostics: " + scratch);
        alert(report.join("\n") + "\n\n" + scratch);
    }
}());
