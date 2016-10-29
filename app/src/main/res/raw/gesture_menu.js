var mode = menu.getMode();
if(mode == Menu.MODE_ITEM_NO_EM || mode == Menu.MODE_ITEM_EM) {
    menu.addMainItem("Edit Gestures", function(v) {
        openApp();
        menu.close();
    });
}

function openApp(){
var screen = getActiveScreen();
var i = new Intent();
i.setClassName("com.faendir.lightning_launcher.multitool", "com.faendir.lightning_launcher.multitool.MainActivity");
i.putExtra("mode", "gesture");
screen.getContext().startActivity(i);
}