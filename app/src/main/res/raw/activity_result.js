eval(loadRawResource("com.faendir.lightning_launcher.multitool", "library"));

var index = token.indexOf('/');
getObjectFactory().get(index === -1 ? token : token.substring(0, index)).create(resultCode, data, index === -1 ? null : token.substring(index + 1));