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
LL.bindClass("android.app.ProgressDialog");
LL.bindClass("android.os.Environment");
LL.bindClass("java.lang.reflect.Array");
LL.bindClass("java.lang.Byte");

function write(stream,file,pathLength,pathPrefix){
if(file.isDirectory()){
var files=file.listFiles();
for(var i=0;i<files.length;i++)write(stream,files[i],pathLength,pathPrefix);
}else if(file.exists()){
var fi = new FileInputStream(file.getPath());
writeStream(stream,fi,pathPrefix + getRelativePath(file, pathLength));
}
}

function getRelativePath(file, pathLength, pathPrefix){
return file.getPath().substring(pathLength);
}


function writeStream(stream,input,relativePath){
 var origin = new BufferedInputStream(input, buffer.length);
startNext(stream,relativePath);
var count;
while ((count = origin.read(buffer, 0, buffer.length)) != -1) {
stream.write(buffer, 0, count);
}
origin.close();
stream.flush();
stream.closeEntry();
}

function startNext(stream,name){
if(!name.indexOf("/"))name = name.substring(1);
stream.putNextEntry(new ZipEntry(name));
}

var buffer = Array.newInstance(Byte.TYPE,1024);
var c=LL.getContext();

var task=new JavaAdapter(AsyncTask,{
doInBackground:function(ignored){
var outDir=new File(Environment.getExternalStorageDirectory(),"LightningLauncher");
outDir.mkdir();
var outFile=new File(outDir,"Autobackup.lla");
var outStream=new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));
outStream.setMethod(ZipOutputStream.DEFLATED);
outStream.setLevel(0);
var inDir=new File(c.getFilesDir());
startNext(outStream,"version");
var p=new PrintWriter(outStream);
p.append("1");
p.flush();
outStream.closeEntry();
startNext(outStream,"core/");
outStream.closeEntry();
var pages=new File(inDir,"pages");
write(outStream,pages,inDir.getPath().length,"core");
var scripts=new File(inDir,"scripts");
write(outStream,scripts,inDir.getPath().length,"core");
var themes=new File(inDir,"themes");
write(outStream,themes,inDir.getPath().length,"core");
var config=new File(inDir,"config");
write(outStream,config,inDir.getPath().length,"core");
var state=new File(inDir,"state");
write(outStream,state,inDir.getPath().length,"core");
var statistics=new File(inDir,"statistics");
write(outStream,statistics,inDir.getPath().length,"core");
startNext(outStream,"wallpaper/");
outStream.closeEntry();
var wp=WallpaperManager.getInstance(c).getDrawable().getBitmap();
startNext(outStream,"wallpaper/bitmap.png");
wp.compress(Bitmap.CompressFormat.PNG,100,outStream);
outStream.flush();
outStream.closeEntry();
startNext(outStream,"fonts/");
outStream.closeEntry();
var fonts=new File(inDir,"fonts");
write(outStream,fonts,inDir.getPath().length,"");
startNext(outStream,"widgets_data/");
outStream.closeEntry();
p.close();
},
onPostExecute:function(ignored){
Toast.makeText(c,"Automatic Lightning Backup completed",Toast.LENGTH_SHORT).show();
}
});

task.execute();
return "";


