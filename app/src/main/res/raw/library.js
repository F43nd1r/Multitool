function getObjectFactory() {
    var packageContext = getActiveScreen().getContext().createPackageContext("com.faendir.lightning_launcher.multitool", 3);
    var factory = packageContext.getClassLoader().loadClass("com.faendir.lightning_launcher.multitool.util.LightningObjectFactory").newInstance();
    factory.init(javaEval, asFunc);
    return factory;
}

function javaEval(name, params) {
    var args = [];
    for (var i = 0; i < params.length; i++) {
        args.push(params[i]);
    }
    return eval(name).apply(self, args);
}

function asFunc(target) {
    return function (a) {
            target.invoke(a);
    }
}