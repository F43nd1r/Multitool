function getMultiToolPackage(){
    return "com.faendir.lightning_launcher.multitool";
}

function installScript(pathSuffix, id, name) {
    var path = '/' + getMultiToolPackage().replace(/\./g, '/') + '/' + pathSuffix;
    var script = getScriptByPathAndName(path, name);
    var script_text = loadRawResource(getMultiToolPackage(), id);
    if (script == null) {
        script = createScript(path, name, script_text, 0);
    } else {
        script.setText(script_text);
    }
    return script;
}

function loadMultiToolClass(relativeClassName) {
    var packageContext = getActiveScreen().getContext().createPackageContext(getMultiToolPackage(), 3);
    return packageContext.getClassLoader().loadClass(getMultiToolPackage() + "." + relativeClassName);
}

function getObjectFactory() {
    return loadMultiToolClass("util.LightningObjectFactory").newInstance();
}

function bindPrefs(keys) {
    var resolver = getActiveScreen().getContext().getContentResolver();
    var result = {};
    var cursor = resolver.query(Uri.parse("content://com.faendir.lightning_launcher.multitool.provider/pref"), null, null, keys, null);
    while (cursor.moveToNext()) {
        result[cursor.getString(0)] = cursor.getString(1);
    }
    cursor.close();
    return result;
}

function setPref(key, value) {
    bindClass("android.content.ContentValues");
    var resolver = getActiveScreen().getContext().getContentResolver();
    var result = {};
    var values = new ContentValues();
    values.put(key, value);
    resolver.update(Uri.parse("content://com.faendir.lightning_launcher.multitool.provider/pref"), values, null, null);
}

function addEventHandler(properties, key, eventHandler) {
    var old = properties.getEventHandler(key);
    eventHandler.setNext(old);
    properties.edit().setEventHandler(key, eventHandler).commit();
}

function centerOnTouch(item) {
    bindClass("java.lang.Integer");
    var screen = getActiveScreen();
    // use the last screen touch position, if any, as location for the new item
    var x = screen.getLastTouchX();
    var y = screen.getLastTouchY();
    var width = item.getWidth();
    var height = item.getHeight();
    if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE) {
        // no previous touch event, use a default position (can happen when using the hardware menu key for instance)
        x = width;
        y = height;
    } else {
        // center around the touch position
        x -= width / 2;
        y -= height / 2;
    }
    item.setPosition(x, y);
}

function javaEval(s, params) {
    var args =[];
    for(var i= 0; i < params.length; i++){
        args.push(params[i]);
    }
    return eval(s).apply(self, args);
}