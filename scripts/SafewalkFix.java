void onPreUpdate() {
    if(client.getPlayer().getPitch() >= 70) {
        modules.enable("SafeWalk");
    } else {
        modules.disable("SafeWalk");
    }
}