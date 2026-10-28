// Generic authoring preview. Component implementations come from the input SWF.
_global.isLivePreview = true;
Stage.align = "TL";
Stage.scaleMode = "noScale";
var previewText = "";
var previewPosition = 0;

function skipPreviewSpace() {
    while (previewPosition < previewText.length && previewText.charCodeAt(previewPosition) <= 32) {
        previewPosition++;
    }
}

function readPreviewValue() {
    skipPreviewSpace();
    var token = previewText.charAt(previewPosition++);
    var value;
    var key;
    var character;
    if (token == "{" || token == "[") {
        var isArray = token == "[";
        value = isArray ? [] : {};
        var closing = isArray ? "]" : "}";
        skipPreviewSpace();
        while (previewPosition < previewText.length && previewText.charAt(previewPosition) != closing) {
            if (!isArray) {
                skipPreviewSpace();
                character = previewText.charAt(previewPosition);
                if (character == "'" || character == '"') {
                    key = readPreviewValue();
                } else {
                    key = "";
                    while (previewPosition < previewText.length && previewText.charAt(previewPosition) != ":") {
                        key += previewText.charAt(previewPosition++);
                    }
                }
                previewPosition++;
            }
            var previous = previewPosition;
            var child = readPreviewValue();
            if (isArray) {
                value.push(child);
            } else {
                value[key] = child;
            }
            skipPreviewSpace();
            if (previewPosition <= previous || previewText.charAt(previewPosition) != ",") {
                break;
            }
            previewPosition++;
        }
        previewPosition++;
        return value;
    }
    if (token == "'" || token == '"') {
        value = "";
        while (previewPosition < previewText.length) {
            character = previewText.charAt(previewPosition++);
            if (character == token) {
                break;
            }
            if (character == "\\") {
                character = previewText.charAt(previewPosition++);
                if (character == "n") character = "\n";
                else if (character == "r") character = "\r";
                else if (character == "t") character = "\t";
            }
            value += character;
        }
        return value;
    }
    value = token;
    while (previewPosition < previewText.length) {
        character = previewText.charAt(previewPosition);
        if (character == "," || character == "}" || character == "]" || character.charCodeAt(0) <= 32) break;
        value += character;
        previewPosition++;
    }
    if (value == "true") return true;
    if (value == "false") return false;
    if (value == "null") return null;
    if (value.charAt(0) == "#") value = "0x" + value.substring(1);
    return Number(value);
}

function currentPreview() {
    if (typeof _root.instanceID == "undefined") return previewComponent;
    var name = "previewInstance" + _root.instanceID;
    if (!_root[name]) previewComponent.duplicateMovieClip(name, Number(_root.instanceID) + 100);
    return _root[name];
}

function onSizeMulti() {
    var component = currentPreview();
    var width = typeof _root.instanceID == "undefined" ? _root.previewWidth : Number(_root.instanceWidth);
    var height = typeof _root.instanceID == "undefined" ? _root.previewHeight : Number(_root.instanceHeight);
    if (component.setSize && width > 0 && height > 0) component.setSize(width, height);
}

function onDrawMulti() {
    if (_root.visiblePreview) _root.visiblePreview._visible = false;
    previewComponent._visible = false;
    _root.visiblePreview = currentPreview();
    _root.visiblePreview._visible = true;
}

function onUpdate() {
    var component = currentPreview();
    for (var key in xch) {
        var name = key.indexOf("def_") == 0 ? key.substring(4) : key;
        component[name] = xch[key];
    }
    if (_root.flashlet) {
        previewText = unescape(_root.flashlet);
        previewPosition = 0;
        var values = readPreviewValue();
        for (var property in values) component[property] = values[property];
    }
    if (component.onUpdate) component.onUpdate();
    onSizeMulti();
    onDrawMulti();
}

function onUpdateMulti() {
    onUpdate();
}

var previewListener = {};
previewListener.onResize = onSizeMulti;
Stage.addListener(previewListener);
onLoad = onUpdate;
