/* Publish real decompiler exports of measured native wave TimeMaps in CS6. */
(function () {
    var onlyCase = null;
    var root = fl.scriptURI.substring(0, fl.scriptURI.lastIndexOf("/") + 1);
    var libraryRoot = root.replace(/motion_object\/$/, "").replace(/testdata\/$/, "");
    var scratch = libraryRoot + "build/motion-diagnostic/native-wave-roundtrip-" + new Date().getTime() + "/";
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
    function validate(id, type, strength, phase) {
        var timeline = owned.getTimeline(), frame = timeline.layers[0].frames[0];
        if (timeline.frameCount !== (strength === 6 ? 361 : 601) || Number(owned.frameRate) !== (strength === 6 ? 24 : 30)
                || Number(owned.width) !== 1600 || Number(owned.height) !== 360) { throw new Error("Unexpected metadata"); }
        if (type === "BounceIn" && strength === 1) {
            if (!frame.isMotionObject()) {
                if (frame.tweenType !== "motion") { throw new Error("Equivalent quadratic tween lost"); }
                return;
            }
            type = "Quadratic";
            strength = -100;
        }
        if (!frame.isMotionObject()) { throw new Error("Motion object missing"); }
        var xml = String(frame.getMotionObjectXML());
        if (!FLfile.write(scratch + id + "/" + phase + "_motion.xml", xml)) { throw new Error("Cannot capture native XML"); }
        var maps = xml.match(/<TimeMap\b[^>]*>/g) || [];
        var properties = xml.match(/<Property\b[^>]*>[\s\S]*?<\/Property>/g) || [];
        for (var p = 0; p < properties.length; p++) {
            var property = properties[p], tag = property.substring(0, property.indexOf(">") + 1);
            if (attr(tag, "id") !== "Motion_X") { continue; }
            var map = maps[Number(attr(tag, "TimeMapIndex") || 0)];
            if (!map || attr(map, "type") !== type || Number(attr(map, "strength")) !== strength
                    || attr(tag, "ignoreTimeMap") !== "0" || (property.match(/<Keyframe\b/g) || []).length !== 2) {
                throw new Error("Native map assignment or base keys changed");
            }
            return;
        }
        throw new Error("Motion_X missing");
    }
    function closeOwned() { if (owned) { var doc = owned; owned = null; fl.closeDocument(doc, false); } }
    try {
        folder(libraryRoot + "build/"); folder(libraryRoot + "build/motion-diagnostic/");
        if (FLfile.exists(scratch) || !FLfile.createFolder(scratch)) { throw new Error("Cannot create fresh run"); }
        var types = ["spring", "bounce", "bounce_in"], names = ["Spring", "Bounce", "BounceIn"], strengths = [1, 2, 3, 4, 5, 6, 8];
        for (var t = 0; t < types.length; t++) {
            for (var s = 0; s < strengths.length; s++) {
                var strength = strengths[s], id = types[t] + "_s" + strength + (strength === 6 ? "_validation" : "_601"), result;
                if (onlyCase !== null && id !== onlyCase) { continue; }
                try {
                    checkpoint(id + ": copying decompiler export");
                    copy(root + "native_wave_exports/" + id + "/", scratch + id + "/");
                    checkpoint(id + ": opening decompiled XFL");
                    owned = fl.openDocument(scratch + id + "/" + id + ".xfl");
                    if (!owned) { throw new Error("Cannot open decompiled XFL"); }
                    validate(id, names[t], strength, "imported");
                    checkpoint(id + ": saving native FLA");
                    var fla = scratch + id + "/native.fla";
                    if (!fl.saveDocument(owned, fla)) { throw new Error("Cannot save native FLA"); }
                    closeOwned();
                    checkpoint(id + ": reopening native FLA");
                    owned = fl.openDocument(fla);
                    if (!owned) { throw new Error("Cannot reopen native FLA"); }
                    validate(id, names[t], strength, "persisted");
                    checkpoint(id + ": exporting SWF");
                    var output = scratch + id + "/published.swf";
                    owned.exportSWF(output, true);
                    if (!FLfile.exists(output)) { throw new Error("SWF missing"); }
                    if (!FLfile.copy(output, root + "native_wave_exports/" + id + "/" + id + "_compiled.swf")) { throw new Error("Cannot retain compiled SWF"); }
                    result = "OK " + id;
                } catch (error) { result = "ERROR " + id + ": " + error; }
                finally { try { closeOwned(); } catch (closeError) { result += "; close failed: " + closeError; } }
                reports.push(result); diagnostic(id + "/RESULT.txt", result + "\r\n");
            }
        }
    } catch (fatal) { reports.push("ERROR: " + fatal); }
    finally {
        try { closeOwned(); } catch (closeError) { reports.push("ERROR closing document: " + closeError); }
        if (active) { fl.setActiveWindow(active); }
        diagnostic("STATUS.txt", reports.join("\r\n") + "\r\n");
        fl.trace(reports.join("\n")); fl.trace("Native wave roundtrip: " + scratch);
        alert(reports.join("\n") + "\n\n" + scratch);
    }
}());
