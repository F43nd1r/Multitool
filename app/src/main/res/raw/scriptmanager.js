if(data != null && data != ""){
    var transfer = JSON.parse(data);
    switch(transfer.request){
        case "RENAME":
            var script = LL.getScriptById(transfer.script.id);
            if(script!=null) script.setName(transfer.script.name);
            break;
        case "DELETE":
            var script = LL.getScriptById(transfer.script.id);
            if(script!=null) LL.deleteScript(script);
            break;
        case "RESTORE":
            var script = LL.getScriptByName(transfer.script.name);
            if(script!=null){
                LL.deleteScript(script);
            }
            LL.createScript(transfer.script.name,transfer.script.code,transfer.script.flags);
            break;
        case "SET_CODE":
            var script = LL.getScriptById(transfer.script.id);
            if(script!=null) script.setText(transfer.script.code);
            break;
    }
}
var s = LL.getAllScriptMatching(Script.FLAG_ALL);
 var list = [];
 for(var i = 0; i < s.length; i++){
     var script = s.getAt(i);
     var flags = 0;
     if(script.hasFlag(Script.FLAG_DISABLED)) flags += Script.FLAG_DISABLED;
     if(script.hasFlag(Script.FLAG_APP_MENU)) flags += Script.FLAG_APP_MENU;
     if(script.hasFlag(Script.FLAG_ITEM_MENU)) flags += Script.FLAG_ITEM_MENU;
     if(script.hasFlag(Script.FLAG_CUSTOM_MENU)) flags += Script.FLAG_CUSTOM_MENU;
      list.push({id:script.getId(), name:script.getName(), code:script.getText(), flags:flags});
 }
return JSON.stringify(list);
