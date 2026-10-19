/* Controlled CS6 measurements. Run with Commands > Run Command. */
(function () {
    var root = fl.scriptURI.substring(0, fl.scriptURI.lastIndexOf("/") + 1);
    var inputs = root + "native_timemap_measurements/";
    var libraryRoot = root.replace(/motion_object\/$/, "").replace(/testdata\/$/, "");
    var scratch = libraryRoot + "build/motion-diagnostic/native-timemap-measurement-" + new Date().getTime() + "/";
    var original = fl.getDocumentDOM(), owned = null, reports = [], warnings = [], step = 0;
    function diagnostic(name, text) {
        var ok = false;
        try { ok = FLfile.write(scratch + name, text); } catch (error) {}
        if (!ok) { warnings.push(name); fl.trace("WARNING: Cannot write diagnostic " + name); }
    }
    function checkpoint(text) {
        diagnostic("STEP-" + ("000" + (++step)).slice(-3) + ".txt", text + "\r\n");
        fl.trace(text);
    }
    function capture(name, text) {
        if (!FLfile.write(scratch + name, text)) { throw new Error("Cannot capture " + name); }
    }
    function folder(uri) {
        if (!FLfile.exists(uri) && !FLfile.createFolder(uri)) { throw new Error("Cannot create " + uri); }
    }
    function copy(source, target) {
        // Every run and case must be fresh; never merge with previous outputs.
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
    function validate(c, phase) {
        var timeline = owned.getTimeline();
        if (timeline.frameCount !== c.frames || Number(owned.frameRate) !== c.fps
                || Number(owned.width) !== 1600 || Number(owned.height) !== 360) {
            throw new Error("Metadata changed during " + phase);
        }
        var frame = timeline.layers[0].frames[0];
        if (!frame.isMotionObject()) { throw new Error("Motion object missing during " + phase); }
        var xml = String(frame.getMotionObjectXML());
        capture(c.id + "/" + phase + "_motion.xml", xml);
        var core = /<AnimationCore\b[^>]*>/.exec(xml);
        if (!core || Number(attr(core[0], "duration")) !== c.frames * 1000
                || Number(attr(core[0], "TimeScale")) !== c.fps * 1000) {
            throw new Error("AnimationCore timing changed during " + phase);
        }
        var maps = xml.match(/<TimeMap\b[^>]*>/g) || [];
        var properties = xml.match(/<Property\b[^>]*>[\s\S]*?<\/Property>/g) || [];
        var found = false;
        for (var p = 0; p < properties.length; p++) {
            var property = properties[p], tag = property.substring(0, property.indexOf(">") + 1);
            if (attr(tag, "id") !== "Motion_X") {
                if (attr(tag, "ignoreTimeMap") === "0") { throw new Error("Unexpected animated TimeMap channel"); }
                continue;
            }
            found = true;
            var map = maps[Number(attr(tag, "TimeMapIndex") || 0)];
            if (attr(tag, "ignoreTimeMap") !== "0" || !map || attr(map, "type") !== c.type
                    || Number(attr(map, "strength")) !== c.strength) {
                throw new Error("Native type/strength assignment changed during " + phase);
            }
            var keys = property.match(/<Keyframe\b[^>]*>/g) || [];
            if (keys.length !== 2 || Number(attr(keys[0], "timevalue")) !== 0
                    || Number(attr(keys[1], "timevalue")) !== (c.frames - 1) * 1000
                    || Number(attr(keys[0], "anchor").split(",")[1]) !== 0
                    || Number(attr(keys[1], "anchor").split(",")[1]) !== 1000) {
                throw new Error("Base Motion_X endpoints changed during " + phase);
            }
            var lastTime = (c.frames - 1) * 1000;
            var next = attr(keys[0], "next").split(","), previous = attr(keys[1], "previous").split(",");
            // Native XML rounds decimal handles; test the slope, not its spelling.
            if (Math.abs(Number(next[0]) - lastTime / 3) > 1
                    || Math.abs(Number(previous[0]) + lastTime / 3) > 1
                    || Math.abs(Number(next[1]) - 1000 / 3) > 0.01
                    || Math.abs(Number(previous[1]) - 2000 / 3) > 0.01) {
                throw new Error("Linear base Motion_X handles changed during " + phase);
            }
        }
        if (!found) { throw new Error("Motion_X missing during " + phase); }
    }
    function publish(c, name) {
        var output = scratch + c.id + "/" + name + ".swf";
        checkpoint(c.id + ": exporting " + name);
        if (FLfile.exists(output)) { throw new Error("Output already exists: " + output); }
        owned.exportSWF(output, true);
        if (!FLfile.exists(output)) { throw new Error("Missing SWF: " + output); }
    }
    function author(c) {
        var timeline = owned.getTimeline();
        timeline.currentLayer = 0; timeline.currentFrame = 0;
        timeline.setSelectedFrames(0, 1, true);
        if (!timeline.layers[0].frames[0].isMotionObject()) { throw new Error("Verified seed lost its motion object"); }
        owned.width = 1600; owned.height = 360; owned.frameRate = c.fps;
        owned.selectAll();
        if (owned.selection.length !== 1 || owned.selection[0].elementType !== "instance") {
            throw new Error("Expected one seed instance");
        }
        owned.selection[0].matrix = {a: 1, b: 0, c: 0, d: 1, tx: 100, ty: 160};
        checkpoint(c.id + ": setting native span duration");
        timeline.layers[0].frames[0].setMotionObjectDuration(c.frames, false);
        var xml = FLfile.read(scratch + c.id + "/requested_motion.xml");
        if (!xml || !/<AnimationCore\b/.test(xml)) { throw new Error("Missing requested motion XML"); }
        checkpoint(c.id + ": applying requested motion through native API");
        timeline.layers[0].frames[0].setMotionObjectXML(xml, false);
    }
    function closeOwned() {
        if (owned) { var closing = owned; owned = null; fl.closeDocument(closing, false); }
    }
    try {
        folder(libraryRoot + "build/"); folder(libraryRoot + "build/motion-diagnostic/");
        if (FLfile.exists(scratch) || !FLfile.createFolder(scratch)) { throw new Error("Cannot create fresh run directory"); }
        var manifest = FLfile.read(inputs + "manifest.js");
        if (!manifest) { throw new Error("Missing measurement manifest"); }
        eval(manifest); // Local generated manifest, compatible with CS6's ES3 runtime.
        for (var i = 0; i < nativeTimeMapMeasurements.length; i++) {
            var c = nativeTimeMapMeasurements[i], result;
            try {
                checkpoint(c.id + ": copying input (" + c.split + ")");
                copy(inputs + c.id + "/", scratch + c.id + "/");
                // Do not feed the rejected generated XFL to the CS6 importer.
                // This seed is byte-identical to the previously verified example.
                copy(root + "47_native_quadratic/", scratch + c.id + "/seed/");
                checkpoint(c.id + ": opening verified seed XFL");
                owned = fl.openDocument(scratch + c.id + "/seed/47_native_quadratic.xfl");
                if (!owned) { throw new Error("Cannot open verified seed XFL"); }
                author(c);
                validate(c, "imported");
                var random = c.type === "RandomSquareWave";
                publish(c, random ? "same_session_1" : "imported");
                if (random) { publish(c, "same_session_2"); }
                checkpoint(c.id + ": saving native FLA");
                var fla = scratch + c.id + "/native.fla";
                if (!fl.saveDocument(owned, fla)) { throw new Error("Cannot save native FLA"); }
                closeOwned();
                checkpoint(c.id + ": reopening native FLA");
                owned = fl.openDocument(fla);
                if (!owned) { throw new Error("Cannot reopen native FLA"); }
                validate(c, "persisted");
                publish(c, random ? "reopened_1" : "persisted");
                if (random) { publish(c, "reopened_2"); }
                result = "OK " + c.id;
            } catch (error) {
                result = "ERROR " + c.id + ": " + error;
            } finally {
                try { closeOwned(); } catch (closeError) { result += "; close failed: " + closeError; }
            }
            reports.push(result); diagnostic(c.id + "/RESULT.txt", result + "\r\n");
        }
    } catch (fatal) {
        reports.push("ERROR: " + fatal);
    } finally {
        try { closeOwned(); } catch (closeError) { reports.push("ERROR closing document: " + closeError); }
        if (original) { fl.setActiveWindow(original); }
        diagnostic("STATUS.txt", reports.join("\r\n") + "\r\n");
        var ok = 0;
        for (var r = 0; r < reports.length; r++) { if (reports[r].indexOf("OK ") === 0) { ok++; } }
        fl.trace(reports.join("\n")); fl.trace("Measurements: " + scratch);
        alert("Native TimeMap measurements: " + ok + " cases succeeded.\n"
                + (warnings.length ? "Diagnostic warnings: " + warnings.length + "\n" : "")
                + "See STATUS.txt and the Output panel for failures.\n\n" + scratch);
    }
}());
