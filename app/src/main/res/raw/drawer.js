eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

bindClass("android.graphics.Bitmap");
bindClass("android.graphics.drawable.BitmapDrawable");
bindClass("android.graphics.Canvas");
bindClass("android.util.Log");
bindClass("java.util.ArrayList");

var c = getEvent().getContainer();
var context = getActiveScreen().getContext();
var pm = context.getPackageManager();

function getPresentActivities() {
    var result = new ArrayList();
    search(c, function(item) {
        var tag = item.getTag("intent");
        if (tag != null) result.add(ComponentName.unflattenFromString(tag));
    });
    return result;
}

function search(container, resultFunc) {
    var items = container.getAllItems();
    for (var i = 0; i < items.length; i++) {
        var item = items[i];
        var type = item.getType();
        if (type == "Panel" || type == "Folder") {
            search(item.getContainer(), resultFunc);
        } else if (type == "Shortcut") {
            resultFunc(item);
        }
    }
}

function getCurrentActivities() {
    var intent = new Intent(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);
    return pm.queryIntentActivities(intent, 0);
}

function toBitmap(drawable) {
    if (drawable instanceof BitmapDrawable) return drawable.getBitmap();
    var bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    var canvas = new Canvas(bmp);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bmp;
}

function Matrix() {
    var m = [];
    this.mark = function(x, y) {
        if (m[x] == null) {
            m[x] = [];
        }
        m[x][y] = true;
    }
    this.get = function(x, y) {
        return m[x] != null && m[x][y];
    }
}

var old = getPresentActivities();
var current = getCurrentActivities();
prefs = bindPrefs(["keepSorted", "hiddenApps"]);
var hidden = JSON.parse(prefs.hiddenApps);
for (var x = 0; x < current.size(); x++) {
    var app = current.get(x);
    var activity = app.activityInfo;
    var name = new ComponentName(activity.packageName, activity.name);
    if(hidden != null && hidden.indexOf(name.flattenToString())!=-1){
        continue;
    }
    if (old.contains(name)) {
        old.remove(name);
        continue;
    }
    var intent = new Intent(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);
    intent.setClassName(activity.packageName, activity.name);
    var item = c.addShortcut(app.loadLabel(pm), intent, 0, 0);
    item.setTag("intent", name.flattenToString());
    var bmp = toBitmap(app.loadIcon(pm));
    var img = Image.createImage(bmp.getWidth(), bmp.getHeight());
    img.draw().drawBitmap(bmp, 0, 0, null);
    item.setDefaultIcon(img);
}
for (var x = 0; x < old.size(); x++) {
    var name = old.get(x);
    var flat = name.flattenToString();
    search(c, function(item) {
        var tag = item.getTag("intent");
        if (tag == flat) {
            item.getParent().removeItem(item);
        }
    });
}
if (JSON.parse(prefs.keepSorted)) {
    setTimeout(function(){deepSort(c);},0);
}

function deepSort(container) {
    var items = container.getAllItems();
    var matrix = new Matrix();
    var move = [];
    for (var i = 0; i < items.length; i++) {
        var item = items[i];
        var type = item.getType();
        if (type == "Panel" || type == "Folder") {
            deepSort(item.getContainer());
        } else if (type == "Shortcut") {
            var tag = item.getTag("intent");
            if (tag != null) move.push(item);
        }
        if (move.indexOf(item) == -1 && item.getProperties().getBoolean("i.onGrid")) {
            var cell = item.getCell();
            for (var x = cell.getLeft(); x < cell.getRight(); x++) {
                for (var y = cell.getTop(); y < cell.getBottom(); y++) {
                    matrix.mark(x, y);
                }
            }
        }
    }
    sortByName(move);
    var width = Math.round(container.getWidth() / container.getCellWidth());
    var x = 0;
    var y = 0;
    for (var i = 0; i < move.length; i++) {
        var item = move[i];
        while (matrix.get(x, y)) {
            if (++x >= width) {
                x = 0;
                y++;
            }
        }
        matrix.mark(x, y);
        item.setCell(x, y, x + 1, y + 1, true);
    }
}

function sortByName(items) {
    items.sort(function(a, b) {
        var na = a.getLabel();
        var nb = b.getLabel();
        return ((na == nb) ? 0 : ((na > nb) ? 1 : -1));
    })
}