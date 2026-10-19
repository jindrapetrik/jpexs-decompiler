/* Native CS6 roundtrip of actual decompiled polynomial TimeMap exports. */
(function () {
    var root = fl.scriptURI.substring(0, fl.scriptURI.lastIndexOf("/") + 1);
    var libraryRoot = root.replace(/motion_object\/$/, "").replace(/testdata\/$/, "");
    var scratch = libraryRoot + "build/motion-diagnostic/native-polynomial-roundtrip-" + new Date().getTime() + "/";
    var ids = ["cubic_50", "quartic_50", "quintic_50", "dual_cubic_50", "dual_quartic_50", "dual_quintic_50"];
    var types = ["Cubic", "Quartic", "Quintic", "DualCubic", "DualQuartic", "DualQuintic"];
    var active = fl.getDocumentDOM(), owned = null, reports = [], step = 0;
    function diagnostic(name, text) {
        try { if (FLfile.write(scratch + name, text)) { return; } } catch (error) {}
        fl.trace("WARNING: Cannot write diagnostic " + name);
    }
    function checkpoint(text) {
        diagnostic("STEP-" + ("000" + (++step)).slice(-3) + ".txt", text + "\r\n"); fl.trace(text);
    }
    function folder(uri) {
        if (!FLfile.exists(uri) && !FLfile.createFolder(uri)) { throw new Error("Cannot create " + uri); }
    }
    function copy(source, target) {
        if (FLfile.exists(target) || !FLfile.createFolder(target)) { throw new Error("Cannot create fresh " + target); }
        var files = FLfile.listFolder(source, "files"), dirs = FLfile.listFolder(source, "directories");
        if (!files || !dirs) { throw new Error("Cannot list " + source); }
        for (var f = 0; f < files.length; f++) {
            if (!FLfile.copy(source + files[f], target + files[f])) { throw new Error("Cannot copy " + files[f]); }
        }
        for (var d = 0; d < dirs.length; d++) { copy(source + dirs[d] + "/", target + dirs[d] + "/"); }
    }
    function attr(tag, name) {
        var match = new RegExp("\\b" + name + '="([^"]*)"').exec(tag);
        return match ? match[1] : null;
    }
    function validate(id, type, phase) {
        var timeline = owned.getTimeline(), frame = timeline.layers[0].frames[0];
        if (timeline.frameCount !== 61 || Number(owned.frameRate) !== 30
                || Number(owned.width) !== 640 || Number(owned.height) !== 360) { throw new Error("Metadata changed"); }
        if (!frame.isMotionObject()) { throw new Error("Motion object missing"); }
        var xml = String(frame.getMotionObjectXML());
        if (!FLfile.write(scratch + id + "/" + phase + "_motion.xml", xml)) { throw new Error("Cannot capture XML"); }
        var core = /<AnimationCore\b[^>]*>/.exec(xml);
        if (!core || Number(attr(core[0], "duration")) !== 61000 || Number(attr(core[0], "TimeScale")) !== 30000) {
            throw new Error("Native core timing changed");
        }
        var maps = xml.match(/<TimeMap\b[^>]*>/g) || [];
        var properties = xml.match(/<Property\b[^>]*>[\s\S]*?<\/Property>/g) || [];
        var expected = {Motion_X: [type, 50], Motion_Y: ["Quadratic", 65], Scale_X: ["Quadratic", -40]};
        for (var name in expected) {
            var found = false;
            for (var p = 0; p < properties.length; p++) {
                var property = properties[p], tag = property.substring(0, property.indexOf(">") + 1);
                if (attr(tag, "id") !== name) { continue; }
                var map = maps[Number(attr(tag, "TimeMapIndex") || 0)], keys = property.match(/<Keyframe\b[^>]*>/g) || [];
                if (!map || attr(map, "type") !== expected[name][0] || Number(attr(map, "strength")) !== expected[name][1]
                        || attr(tag, "ignoreTimeMap") !== "0" || keys.length !== 2
                        || Number(attr(keys[0], "timevalue")) !== 0 || Number(attr(keys[1], "timevalue")) !== 60000) {
                    throw new Error("Native assignment/base keys changed for " + name);
                }
                found = true; break;
            }
            if (!found) { throw new Error("Missing " + name); }
        }
    }
    function closeOwned() { if (owned) { var doc = owned; owned = null; fl.closeDocument(doc, false); } }
    try {
        folder(libraryRoot + "build/"); folder(libraryRoot + "build/motion-diagnostic/");
        if (FLfile.exists(scratch) || !FLfile.createFolder(scratch)) { throw new Error("Cannot create fresh run"); }
        for (var i = 0; i < ids.length; i++) {
            var id = ids[i], result;
            try {
                checkpoint(id + ": copying actual decompiler export");
                copy(root + "native_polynomial_exports/" + id + "/", scratch + id + "/");
                checkpoint(id + ": opening decompiled XFL");
                owned = fl.openDocument(scratch + id + "/" + id + ".xfl");
                if (!owned) { throw new Error("Cannot open XFL"); }
                validate(id, types[i], "imported");
                checkpoint(id + ": saving native FLA");
                var fla = scratch + id + "/native.fla";
                if (!fl.saveDocument(owned, fla)) { throw new Error("Cannot save native FLA"); }
                closeOwned();
                checkpoint(id + ": reopening native FLA");
                owned = fl.openDocument(fla);
                if (!owned) { throw new Error("Cannot reopen native FLA"); }
                validate(id, types[i], "persisted");
                checkpoint(id + ": exporting SWF");
                var output = scratch + id + "/published.swf";
                owned.exportSWF(output, true);
                if (!FLfile.exists(output)) { throw new Error("SWF missing"); }
                var retained = root + "native_polynomial_exports/" + id + "/" + id + "_compiled.swf";
                if (FLfile.exists(retained)) {
                    if (!FLfile.copy(retained, scratch + id + "/previous_compiled.swf") || !FLfile.remove(retained)) {
                        throw new Error("Cannot back up previous compiled SWF");
                    }
                }
                if (!FLfile.copy(output, retained)) { throw new Error("Cannot retain compiled SWF; diagnostic output is available"); }
                result = "OK " + id;
            } catch (error) { result = "ERROR " + id + ": " + error; }
            finally { try { closeOwned(); } catch (closeError) { result += "; close failed: " + closeError; } }
            reports.push(result); diagnostic(id + "/RESULT.txt", result + "\r\n");
        }
    } catch (fatal) { reports.push("ERROR: " + fatal); }
    finally {
        try { closeOwned(); } catch (closeError) { reports.push("ERROR closing document: " + closeError); }
        if (active) { fl.setActiveWindow(active); }
        diagnostic("STATUS.txt", reports.join("\r\n") + "\r\n");
        fl.trace(reports.join("\n")); fl.trace("Native polynomial roundtrip: " + scratch);
        alert(reports.join("\n") + "\n\n" + scratch);
    }
}());
