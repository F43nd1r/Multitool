package com.faendir.lightning_launcher.multitool.scriptmanager;

import com.faendir.lightning_launcher.multitool.proxy.JavaScript;
import com.faendir.lightning_launcher.multitool.proxy.Utils;
import com.faendir.lightning_launcher.multitool.proxy.Script;

import java.util.ArrayList;
import java.util.List;

import static com.faendir.lightning_launcher.multitool.util.Utils.GSON;

/**
 * @author lukas
 * @since 09.07.18
 */
public class ScriptManager implements JavaScript.Direct {
    private final Utils utils;

    public ScriptManager(Utils utils) {
        this.utils = utils;
    }

    @Override
    public String execute(String data) {
        if (data != null && !"".equals(data)) {
            Transfer transfer = GSON.fromJson(data, Transfer.class);
            Script script;
            switch (transfer.request) {
                case Transfer.RENAME:
                    script = utils.getLightning().getScriptById(String.valueOf(transfer.script.getId()));
                    if (script != null) script.setName(transfer.script.getName());
                    break;
                case Transfer.DELETE:
                    script = utils.getLightning().getScriptById(String.valueOf(transfer.script.getId()));
                    if (script != null) utils.getLightning().deleteScript(script);
                    break;
                case Transfer.RESTORE:
                    String path = transfer.script.getPath() != null ? transfer.script.getPath() : "/";
                    script = utils.getLightning().getScriptByPathAndName(path, transfer.script.getName());
                    if (script != null) {
                        utils.getLightning().deleteScript(script);
                    }
                    utils.getLightning().createScript(path, transfer.script.getName(), transfer.script.getCode(), transfer.script.getFlags());
                    break;
                case Transfer.SET_CODE:
                    script = utils.getLightning().getScriptById(String.valueOf(transfer.script.getId()));
                    if (script != null) script.setText(transfer.script.getCode());
                    break;
                case Transfer.TOGGLE_DISABLE:
                    script = utils.getLightning().getScriptById(String.valueOf(transfer.script.getId()));
                    if (script != null) script.setFlag(Script.FLAG_DISABLED, !script.hasFlag(Script.FLAG_DISABLED));
                    break;
            }
        }
        Script[] scripts = utils.getLightning().getAllScriptMatching(Script.FLAG_ALL);
        List<com.faendir.lightning_launcher.multitool.scriptmanager.Script> list = new ArrayList<>();
        for (Script script : scripts){
            int flags = 0;
            if (script.hasFlag(Script.FLAG_DISABLED)) flags += Script.FLAG_DISABLED;
            if (script.hasFlag(Script.FLAG_APP_MENU)) flags += Script.FLAG_APP_MENU;
            if (script.hasFlag(Script.FLAG_ITEM_MENU)) flags += Script.FLAG_ITEM_MENU;
            if (script.hasFlag(Script.FLAG_CUSTOM_MENU)) flags += Script.FLAG_CUSTOM_MENU;
            list.add(new com.faendir.lightning_launcher.multitool.scriptmanager.Script(script.getName(), script.getId(), script.getText(), flags, script.getPath()));
        }
        return GSON.toJson(list);
    }
}
