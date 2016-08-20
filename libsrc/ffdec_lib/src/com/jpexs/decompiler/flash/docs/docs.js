function bind(el, evt, func) {
    if (el.addEventListener) {
        el.addEventListener(evt, func, false);
    } else if (el.attachEvent) {
        el.attachEvent('on' + evt, func);
    }
}

function showhide(e, show) {
    var cls = " " + e.className + " ";
    if (show && cls.indexOf(" hidden ") > -1) {
        cls = cls.replace(" hidden ", " ");
    } else if (!show && cls.indexOf(" hidden ") == -1) {
        cls = cls + "hidden ";
    }
    cls = cls.trim();
    e.className = cls;
}

function hideNoName(name) {
    var lis = document.getElementsByTagName("li");
    loopi:for (var i = 0; i < lis.length; i++) {
        if (lis[i].className == "instruction-item") {
            if ((typeof name) != "undefined" && name.trim() != "") {
                var ss = lis[i].getElementsByTagName("strong");
                for (var s = 0; s < ss.length; s++) {
                    if (ss[s].className == "instruction-name")
                    {
                        var insName = ss[s].innerHTML;
                        if (insName.toLowerCase().indexOf(name.toLowerCase()) != 0) { //does not start with desired name
                            showhide(lis[i], false);
                            continue loopi;
                        }
                    }
                }
            }
        }
    }
}

function showFlags(show, flagType) {
    var lis = document.getElementsByTagName("div");
    loopi:for (var i = 0; i < lis.length; i++) {
        var cls = " " + lis[i].className + " ";
        if ((typeof flagType) != "undefined") {
            if (cls.indexOf("instruction-flag-" + flagType) == -1) {
                continue;
            }
        }
        showhide(lis[i].parentNode, show);
    }
}


function sortInstructions(order) {
    var newUl = document.createElement("ul");
    newUl.className = "instruction-list";


    var smallestItem = null;
    var smallestVal = null;
    var originalUl = null;

    do {
        smallestItem = null;
        smallestVal = null;
        var lis = document.getElementsByTagName("li");
        loopi:for (var i = 0; i < lis.length; i++) {
            var cls = " " + lis[i].className + " ";
            if (cls.indexOf(" instruction-item ") != -1) {
                var ss = lis[i].getElementsByTagName(order == "code" ? "span" : "strong");
                for (var s = 0; s < ss.length; s++) {
                    if (ss[s].className == "instruction-" + order)
                    {
                        var checkedVal = ss[s].innerHTML;
                        if (smallestVal == null || smallestVal > checkedVal)
                        {
                            smallestItem = lis[i];
                            smallestVal = checkedVal;
                        }
                        break;
                    }
                }
            }
        }
        if (smallestItem != null) {
            originalUl = smallestItem.parentNode;
            originalUl.removeChild(smallestItem);
            newUl.appendChild(smallestItem);
        }
    } while (smallestItem != null);
    originalUl.parentNode.replaceChild(newUl, originalUl);

}

function applyFilter() {
    var order_new = document.getElementById("filter_order").value;
    if (order_set != order_new) {
        order_set = order_new;
        sortInstructions(order_new);
    }
    showFlags(true);
    hideNoName(document.getElementById("filter-byname").value);
    var inputs = document.getElementsByTagName("input");
    for (var i = 0; i < inputs.length; i++) {
        if (inputs[i].type == "checkbox" && inputs[i].className == "filter" && inputs[i].hasAttribute("data-flag")) {
            var flag = inputs[i].getAttribute("data-flag");
            if (inputs[i].checked) {
                showFlags(false, flag);
            }
        }
    }
}

function init() {
    var js_switcher = document.getElementById("js-switcher");
    if (!js_switcher) {
        return;
    }
    var t = "";
    t += "<div class=\"filter\">";

    t += "<div class=\"filter-item\">";
    t += "<label for=\"filter-byname\"><strong class=\"filter-byname-title\">" + txt_filter_byname + "</strong></label>";
    t += "<input onkeydown=\"applyFilter();\" onkeyup=\"applyFilter();\" onkeypress=\"applyFilter();\" type=\"text\" id=\"filter-byname\" size=\"15\" />";
    t += "</div>";

    t += "<div class=\"filter-item\">";
    t += "<strong class=\"filter-hide-title\">" + txt_filter_hide + "</strong><br />";
    for (var flag in flags) {
        var flagDesc = flags[flag];
        var flagSet = flags_set[flag];
        t += '<input class="filter" data-flag="' + flag + '" onchange="applyFilter();" type="checkbox"' + (flagSet ? ' checked="checked"' : '') + ' id="flag-' + flag + '-switch"/><label for="flag-' + flag + '-switch">' + flagDesc + '</label><br />';
    }

    t += "<div class=\"filter-item\">";
    t += "<label for=\"filter-order\"><strong class=\"filter-order-title\">" + txt_filter_order + "</strong></label>";
    t += "<select id=\"filter_order\" onchange=\"applyFilter();\">";
    t += "<option value=\"code\"" + (order_set == "code" ? ' selected="selected"' : '') + ">" + txt_filter_order_code + "</option>";
    t += "<option value=\"name\"" + (order_set == "name" ? ' selected="selected"' : '') + ">" + txt_filter_order_name + "</option>";
    t += "</select>";
    t += "</div>";
    t += "</div>";

    t += "</div>"; //.filter


    js_switcher.innerHTML = t;
    applyFilter();
}

bind(window, "load", init);