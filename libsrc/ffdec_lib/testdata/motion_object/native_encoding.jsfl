/* Controlled CS6 publishing experiments. Original fixtures are never edited. */
(function () {
    var root = fl.scriptURI.substring(0, fl.scriptURI.lastIndexOf("/") + 1);
    var libraryRoot = root.replace(/motion_object\/$/, "").replace(/testdata\/$/, "");
    var output = libraryRoot + "build/motion-diagnostic/native-encoding-" + new Date().getTime() + "/";
    var ids = ["03_rotation", "05_animated_skew", "16_rotation_skew_dual_quadratic"];
    var variants = ["control", "base_axes", "base_endpoint", "base_quarter_turn",
        "rotation_z", "rotation_z_axes", "angles_six_decimals", "angles_plus_360", "angles_epsilon"];
    var results = [];
    var active = fl.getDocumentDOM();
    function folder(uri) {
        if (!FLfile.exists(uri) && !FLfile.createFolder(uri)) { throw new Error("Cannot create " + uri); }
    }
    function write(uri, value) {
        if (!FLfile.write(uri, value)) { throw new Error("Cannot write " + uri); }
    }
    function copy(source, destination) {
        folder(destination);
        var files = FLfile.listFolder(source, "files");
        var dirs = FLfile.listFolder(source, "directories");
        if (!files || !dirs) { throw new Error("Cannot list " + source); }
        var i;
        for (i = 0; i < files.length; i++) {
            if (!FLfile.copy(source + files[i], destination + files[i])) { throw new Error("Cannot copy " + files[i]); }
        }
        for (i = 0; i < dirs.length; i++) { copy(source + dirs[i] + "/", destination + dirs[i] + "/"); }
    }
    function property(xml, id) {
        var expression = new RegExp('(<Property\\b[^>]*\\bid="' + id + '"[^>]*>)([\\s\\S]*?)(</Property>)');
        var match = expression.exec(xml);
        if (!match) { throw new Error("Missing property " + id); }
        return { expression: expression, body: match[2] };
    }
    function replaceBody(xml, id, body) {
        return xml.replace(property(xml, id).expression, function (all, open, old, close) { return open + body + close; });
    }
    function number(value) {
        // All experimental values are moderate; avoid scientific notation.
        return Number(value.toFixed(12)).toString();
    }
    function mapValues(body, callback) {
        return body.replace(/(anchor|previous|next)="([^,]*),([^"]*)"/g, function (all, attr, x, y) {
            return attr + '="' + x + ',' + number(callback(Number(y))) + '"';
        });
    }
    function keys(body) {
        var result = [], expression = /<Keyframe\b[^>]*\/>/g, match;
        while ((match = expression.exec(body))) {
            var time = /timevalue="([^"]+)"/.exec(match[0]);
            var anchor = /anchor="[^,]*,([^"]+)"/.exec(match[0]);
            if (!time || !anchor || !isFinite(Number(anchor[1]))) { throw new Error("Invalid property key"); }
            result.push({ time: Number(time[1]), value: Number(anchor[1]) });
        }
        if (!result.length) { throw new Error("No property keys"); }
        return result;
    }
    function at(values, time) {
        // Existing angular fixtures have either constant or per-frame keys.
        var value = values[0].value;
        for (var i = 0; i < values.length && values[i].time <= time; i++) { value = values[i].value; }
        return value;
    }
    function canonical(xml) {
        var x = property(xml, "Skew_X"), y = property(xml, "Skew_Y");
        var xs = keys(x.body), ys = keys(y.body), skew = "";
        for (var time = 0; time <= 60000; time += 1000) {
            var point = "0," + number(at(xs, time) - at(ys, time));
            skew += '<Keyframe timevalue="' + time + '" roving="0" anchor="' + point +
                '" previous="' + point + '" next="' + point + '"/>';
        }
        xml = replaceBody(xml, "Rotation_Z", y.body);
        xml = replaceBody(xml, "Skew_X", skew);
        return replaceBody(xml, "Skew_Y", '<Keyframe timevalue="0" roving="0" anchor="0,0" previous="0,0" next="0,0"/>');
    }
    function expected(uri) {
        var source = FLfile.read(uri);
        if (!source) { throw new Error("Missing reference CSV"); }
        var lines = source.replace(/\s+$/, "").split(/\r?\n/), matrices = [];
        if (lines.length !== 62) { throw new Error("Expected 61 reference frames"); }
        for (var f = 1; f < lines.length; f++) {
            var values = lines[f].split(","), matrix = [];
            if (values.length !== 15 || Number(values[0]) !== f - 1) { throw new Error("Invalid reference frame"); }
            for (var i = 1; i <= 6; i++) {
                if (!/^-?\d+$/.test(values[i])) { throw new Error("Invalid matrix"); }
                matrix.push(Number(values[i]) / (i <= 4 ? 65536 : 20));
            }
            matrices.push(matrix);
        }
        return matrices;
    }
    function variantXML(xml, variant, matrices) {
        if (variant === "rotation_z" || variant === "rotation_z_axes") { xml = canonical(xml); }
        if (variant === "angles_six_decimals" || variant === "angles_plus_360" || variant === "angles_epsilon") {
            var transform = function (v) {
                return variant === "angles_six_decimals" ? Number(v.toFixed(6)) :
                    v + (variant === "angles_plus_360" ? 360 : 0.00001);
            };
            xml = replaceBody(xml, "Skew_X", mapValues(property(xml, "Skew_X").body, transform));
            xml = replaceBody(xml, "Skew_Y", mapValues(property(xml, "Skew_Y").body, transform));
        }
        if (variant.indexOf("base_") === 0 || variant === "rotation_z_axes") {
            var first = matrices[0], m = first.slice(0), sx = Math.sqrt(m[0] * m[0] + m[1] * m[1]);
            var sy = Math.sqrt(m[2] * m[2] + m[3] * m[3]) * (m[0] * m[3] - m[1] * m[2] < 0 ? -1 : 1);
            if (variant === "base_endpoint") { m = matrices[matrices.length - 1].slice(0); }
            else if (variant === "base_quarter_turn") { m = [0, sx, -sy, 0]; }
            else { m = [sx, 0, 0, sy]; }
            var replacement = '<Matrix a="' + number(m[0]) + '" b="' + number(m[1]) + '" c="' +
                number(m[2]) + '" d="' + number(m[3]) + '" tx="' + number(first[4]) + '" ty="' + number(first[5]) + '"/>';
            var count = 0;
            xml = xml.replace(/<Matrix\b[^>]*\/>/g, function () { count++; return replacement; });
            if (count !== 1) { throw new Error("Expected one instance matrix"); }
        }
        if (xml.indexOf('tweenType="motion object"') === -1) { throw new Error("Motion object lost"); }
        return xml;
    }
    folder(libraryRoot + "build/");
    folder(libraryRoot + "build/motion-diagnostic/");
    if (FLfile.exists(output)) { throw new Error("Output already exists"); }
    folder(output);
    for (var example = 0; example < ids.length; example++) {
        var id = ids[example];
        folder(output + id + "/");
        for (var v = 0; v < variants.length; v++) {
            var variant = variants[v], doc = null;
            try {
                var destination = output + id + "/" + variant + "/";
                copy(root + id + "/", destination);
                // Keep the original failing motion input available after the
                // production fixture has been replaced by a faithful fallback.
                var historical = destination + "unsafe_motion.xml";
                var xml = FLfile.read(FLfile.exists(historical) ? historical : destination + "DOMDocument.xml");
                if (!xml) { throw new Error("Missing DOMDocument.xml"); }
                write(destination + "DOMDocument.xml", variantXML(xml, variant, expected(root + id + "/expected.csv")));
                doc = fl.openDocument(destination + id + ".xfl");
                if (!doc) { throw new Error("Cannot open XFL"); }
                var timeline = doc.getTimeline(), motion = null;
                for (var layer = 0; layer < timeline.layers.length; layer++) {
                    var frame = timeline.layers[layer].frames[0];
                    if (frame && frame.isMotionObject()) { motion = frame; }
                }
                if (!motion) { throw new Error("No native motion object"); }
                write(destination + "imported_motion.xml", String(motion.getMotionObjectXML()));
                doc.exportSWF(destination + "published.swf", true);
                if (!FLfile.exists(destination + "published.swf")) { throw new Error("SWF export failed"); }
                results.push(id + "/" + variant + ": published");
            } catch (error) { results.push(id + "/" + variant + ": FAILED: " + String(error)); }
            finally { if (doc) { fl.closeDocument(doc, false); } }
            write(output + "STATUS.txt", results.join("\r\n") + "\r\n");
        }
    }
    if (active) { fl.setActiveWindow(active); }
    fl.trace("Native encoding results: " + output);
    alert("Native encoding comparison finished.\n" + results.join("\n") + "\n\n" + output);
}());
