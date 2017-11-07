var event = getEvent();
var musicListener = event.getContainer().my.musicListener;
if (musicListener != null) {
    try {
        switch(parseInt(event.getData())){
            case 5:
                musicListener.sendPlayPause();
                break;
            case 6:
                musicListener.sendNext();
                break;
            case 7:
                musicListener.sendPrevious();
                break;
        }
    } catch (e) {}
}