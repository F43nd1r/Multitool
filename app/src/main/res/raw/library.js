function getObjectFactory() {
    var packageContext = getActiveScreen().getContext().createPackageContext("com.faendir.lightning_launcher.multitool", 3);
    var factory = packageContext.getClassLoader().loadClass("com.faendir.lightning_launcher.multitool.util.LightningObjectFactory").newInstance();
    factory.init(javaEval);
    return factory;
}

function javaEval(target, name, params) {
    bindClass("java.lang.Runnable");
    var args = [];
    for (var i = 0; i < params.length; i++) {
        if (params[i] instanceof Runnable) {
            args.push(function (arg) {
                return function () {
                    arg.run();
                }
            }(params[i]))
        } else {
            args.push(params[i]);
        }
    }
    var func = target == null ? eval(name) : target[name];
    return func.apply(target || self, args);
}