/*
 * Native CS6 probe for angular motion-object conversion.
 * Run with Commands > Run Command. Only fresh diagnostic copies are edited.
 */
(function () {
    var root = fl.scriptURI.substring(0, fl.scriptURI.lastIndexOf("/") + 1);
    var libraryRoot = parent(parent(root));
    var output = libraryRoot + "build/motion-diagnostic/native-authoring-" + new Date().getTime() + "/";
    var ids = ["03_rotation", "05_animated_skew", "16_rotation_skew_dual_quadratic"];
    var results = [];
    var active = fl.getDocumentDOM();

    function parent(uri) {
        var trimmed = uri.replace(/\/$/, "");
        return trimmed.substring(0, trimmed.lastIndexOf("/") + 1);
    }
    function ensureFolder(uri) {
        if (!FLfile.exists(uri) && !FLfile.createFolder(uri)) {
            throw new Error("Cannot create diagnostic folder: " + uri);
        }
    }
    function write(uri, text) {
        if (!FLfile.write(uri, text)) {
            throw new Error("Cannot write diagnostic file: " + uri);
        }
    }
    function copyTree(source, destination) {
        ensureFolder(destination);
        var files = FLfile.listFolder(source, "files");
        var folders = FLfile.listFolder(source, "directories");
        if (!files || !folders) {
            throw new Error("Cannot list XFL folder: " + source);
        }
        var i;
        for (i = 0; i < files.length; i++) {
            if (!FLfile.copy(source + files[i], destination + files[i])) {
                throw new Error("Cannot copy XFL file: " + source + files[i]);
            }
        }
        for (i = 0; i < folders.length; i++) {
            copyTree(source + folders[i] + "/", destination + folders[i] + "/");
        }
    }
    function loadExpected(uri) {
        var text = FLfile.read(uri);
        if (!text) {
            throw new Error("Cannot read expected.csv: " + uri);
        }
        var lines = text.split(/\r?\n/);
        if (lines[0] !== "frameIndex,aFixed16,bFixed16,cFixed16,dFixed16,txTwips,tyTwips,rMultFixed8,gMultFixed8,bMultFixed8,aMultFixed8,rOffset,gOffset,bOffset,aOffset") {
            throw new Error("Unexpected expected.csv header");
        }
        var matrices = [];
        for (var line = 1; line < lines.length; line++) {
            if (/^\s*$/.test(lines[line])) {
                continue;
            }
            var fields = lines[line].split(",");
            if (fields.length !== 15) {
                throw new Error("Invalid expected.csv row " + line);
            }
            var values = [];
            for (var c = 0; c < fields.length; c++) {
                if (!/^-?\d+$/.test(fields[c])) {
                    throw new Error("Non-integer expected.csv value at row " + line);
                }
                values[c] = Number(fields[c]);
            }
            if (values[0] !== matrices.length) {
                throw new Error("Noncontiguous expected.csv frames");
            }
            matrices.push({
                a: values[1] / 65536, b: values[2] / 65536,
                c: values[3] / 65536, d: values[4] / 65536,
                tx: values[5] / 20, ty: values[6] / 20
            });
        }
        if (matrices.length !== 61) {
            throw new Error("Expected 61 fixture frames");
        }
        return matrices;
    }
    function motionLayer(doc) {
        var timeline = doc.getTimeline();
        var found = -1;
        for (var i = 0; i < timeline.layers.length; i++) {
            var first = timeline.layers[i].frames[0];
            if (first && first.isMotionObject()) {
                if (found !== -1) {
                    throw new Error("Expected one motion-object layer");
                }
                found = i;
            }
        }
        if (found === -1) {
            throw new Error("No motion object after native import");
        }
        return found;
    }
    function currentElement(doc, layer, frame) {
        var timeline = doc.getTimeline();
        timeline.currentLayer = layer;
        timeline.currentFrame = frame;
        if (!timeline.layers[layer].frames[frame].isMotionObject()) {
            throw new Error("Frame " + frame + " is no longer a motion object");
        }
        doc.selectAll();
        if (doc.selection.length !== 1 || doc.selection[0].elementType !== "instance") {
            throw new Error("Expected one instance at frame " + frame);
        }
        return doc.selection[0];
    }
    function matrixRow(frame, matrix) {
        return [frame, Math.round(matrix.a * 65536), Math.round(matrix.b * 65536),
            Math.round(matrix.c * 65536), Math.round(matrix.d * 65536),
            Math.round(matrix.tx * 20), Math.round(matrix.ty * 20)].join(",");
    }
    function capture(doc, layer, expected, uri) {
        var rows = ["frameIndex,aFixed16,bFixed16,cFixed16,dFixed16,txTwips,tyTwips"];
        var mismatchFrames = 0;
        for (var frame = 0; frame < expected.length; frame++) {
            var matrix = currentElement(doc, layer, frame).matrix;
            rows.push(matrixRow(frame, matrix));
            var target = expected[frame];
            if (Math.abs(matrix.a - target.a) > 2 / 65536 ||
                    Math.abs(matrix.b - target.b) > 2 / 65536 ||
                    Math.abs(matrix.c - target.c) > 2 / 65536 ||
                    Math.abs(matrix.d - target.d) > 2 / 65536 ||
                    Math.abs(matrix.tx - target.tx) > 0.051 ||
                    Math.abs(matrix.ty - target.ty) > 0.051) {
                mismatchFrames++;
            }
        }
        write(uri, rows.join("\r\n") + "\r\n");
        return mismatchFrames;
    }
    function save(doc, uri) {
        if (!fl.saveDocument(doc, uri) || !FLfile.exists(uri)) {
            throw new Error("Native FLA save failed: " + uri);
        }
    }
    function exportMovie(doc, uri) {
        doc.exportSWF(uri, true);
        if (!FLfile.exists(uri)) {
            throw new Error("Native SWF export failed: " + uri);
        }
    }
    function nativeMotion(doc, layer, uri) {
        var timeline = doc.getTimeline();
        timeline.currentFrame = 0;
        timeline.currentLayer = layer;
        var xml = timeline.layers[layer].frames[0].getMotionObjectXML();
        if (!xml || String(xml).indexOf("AnimationCore") === -1) {
            throw new Error("Native motion XML is missing");
        }
        write(uri, String(xml));
    }
    ensureFolder(libraryRoot + "build/");
    ensureFolder(libraryRoot + "build/motion-diagnostic/");
    if (FLfile.exists(output)) {
        throw new Error("Diagnostic output already exists: " + output);
    }
    ensureFolder(output);
    write(output + "STATUS.txt", "Running native angular conversion probe.\r\n");
    for (var example = 0; example < ids.length; example++) {
        var id = ids[example];
        var doc = null;
        try {
            var expected = loadExpected(root + id + "/expected.csv");
            var caseOutput = output + id + "/";
            ensureFolder(caseOutput);
            copyTree(root + id + "/", caseOutput + "input/");
            var historical = caseOutput + "input/unsafe_motion.xml";
            if (FLfile.exists(historical)) {
                write(caseOutput + "input/DOMDocument.xml", FLfile.read(historical));
            }
            doc = fl.openDocument(caseOutput + "input/" + id + ".xfl");
            if (!doc) {
                throw new Error("Cannot open diagnostic XFL copy");
            }
            fl.setActiveWindow(doc);
            var layer = motionLayer(doc);
            if (doc.getTimeline().layers[layer].frames.length !== expected.length) {
                throw new Error("Native import changed the frame count");
            }
            nativeMotion(doc, layer, caseOutput + "before_motion.xml");
            var before = capture(doc, layer, expected, caseOutput + "before_stage.csv");
            save(doc, caseOutput + "before_native.fla");
            exportMovie(doc, caseOutput + "before_native.swf");
            for (var frame = 0; frame < expected.length; frame++) {
                // Assignment through the native authoring API lets CS6 choose
                // its own scale/skew/rotation representation for this matrix.
                currentElement(doc, layer, frame).matrix = expected[frame];
                currentElement(doc, layer, frame);
            }
            var after = capture(doc, layer, expected, caseOutput + "after_stage.csv");
            nativeMotion(doc, layer, caseOutput + "after_motion.xml");
            save(doc, caseOutput + "after_native.fla");
            fl.closeDocument(doc, false);
            doc = null;
            doc = fl.openDocument(caseOutput + "after_native.fla");
            if (!doc) {
                throw new Error("Cannot reopen native FLA");
            }
            fl.setActiveWindow(doc);
            layer = motionLayer(doc);
            var persisted = capture(doc, layer, expected, caseOutput + "persisted_stage.csv");
            nativeMotion(doc, layer, caseOutput + "persisted_motion.xml");
            exportMovie(doc, caseOutput + "after_native.swf");
            results.push(id + ": completed; stage mismatches before=" + before +
                ", after=" + after + ", persisted=" + persisted +
                ". SWF fidelity still requires comparison.");
        } catch (error) {
            results.push(id + ": FAILED: " + String(error));
        } finally {
            if (doc) {
                fl.closeDocument(doc, false);
            }
        }
        write(output + "STATUS.txt", results.join("\r\n") + "\r\n");
    }
    if (active) {
        fl.setActiveWindow(active);
    }
    fl.trace("Native angular results: " + output);
    alert("Native angular probe finished.\n" + results.join("\n") + "\n\nOutput:\n" + output);
}());