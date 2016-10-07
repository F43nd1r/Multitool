LL.bindClass("java.io.FileInputStream");
LL.bindClass("java.io.FileOutputStream");
LL.bindClass("java.io.BufferedInputStream");
LL.bindClass("java.io.BufferedOutputStream");
LL.bindClass("java.io.File");
LL.bindClass("java.io.PrintWriter");
LL.bindClass("java.util.zip.ZipOutputStream");
LL.bindClass("java.util.zip.ZipEntry");
LL.bindClass("android.app.WallpaperManager");
LL.bindClass("android.graphics.Bitmap");
LL.bindClass("android.os.AsyncTask");
LL.bindClass("android.os.Environment");
LL.bindClass("java.lang.reflect.Array");
LL.bindClass("java.lang.Byte");

function write(stream, file, pathLength, pathPrefix) {
    if (file.isDirectory()) {
        var files = file.listFiles();
        for (var i = 0; i < files.length; i++) {
            write(stream, files[i], pathLength, pathPrefix);
        }
    } else if (file.exists()) {
        var fi = new FileInputStream(file.getPath());
        writeStream(stream, fi, pathPrefix + getRelativePath(file, pathLength));
    }
}

function dir(name) {
    startNext(outStream, name + "/");
    outStream.closeEntry();
}

function getRelativePath(file, pathLength, pathPrefix) {
    return file.getPath().substring(pathLength);
}


function writeStream(stream, input, relativePath) {
    var origin = new BufferedInputStream(input, buffer.length);
    startNext(stream, relativePath);
    var count;
    while ((count = origin.read(buffer, 0, buffer.length)) != -1) {
        stream.write(buffer, 0, count);
    }
    origin.close();
    stream.flush();
    stream.closeEntry();
}

function startNext(stream, name) {
    if (!name.indexOf("/")) name = name.substring(1);
    stream.putNextEntry(new ZipEntry(name));
}

var buffer = Array.newInstance(Byte.TYPE, 1024);
var c = LL.getContext();

var task = new JavaAdapter(AsyncTask, {
    doInBackground: function(ignored) {
        var outDir = new File(Environment.getExternalStorageDirectory(), "LightningLauncher");
        outDir.mkdir();
        var outFile = new File(outDir, "Autobackup.lla");
        var outStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));
        outStream.setMethod(ZipOutputStream.DEFLATED);
        outStream.setLevel(0);
        var inDir = new File(c.getFilesDir());
        startNext(outStream, "version");
        var p = new PrintWriter(outStream);
        p.append("1");
        p.flush();
        outStream.closeEntry();
        dir("core");
        write(outStream, new File(inDir, "pages"), inDir.getPath().length, "core");
        write(outStream, new File(inDir, "scripts"), inDir.getPath().length, "core");
        write(outStream, new File(inDir, "themes"), inDir.getPath().length, "core");
        write(outStream, new File(inDir, "config"), inDir.getPath().length, "core");
        write(outStream, new File(inDir, "state"), inDir.getPath().length, "core");
        write(outStream, new File(inDir, "statistics"), inDir.getPath().length, "core");
        write(outStream, new File(inDir, "system"), inDir.getPath().length, "core");
        write(outStream, new File(inDir, "variables"), inDir.getPath().length, "core");
        dir("wallpaper");
        var wp = WallpaperManager.getInstance(c).getDrawable().getBitmap();
        startNext(outStream, "wallpaper/bitmap.png");
        wp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        outStream.flush();
        outStream.closeEntry();
        dir("fonts");
        write(outStream, new File(inDir, "fonts"), inDir.getPath().length, "");
        dir("widgets_data");
        p.close();
    },
    onPostExecute: function(ignored) {
        Toast.makeText(c, "Automatic Lightning Backup completed", Toast.LENGTH_SHORT).show();
    }
});

task.execute();
return "";