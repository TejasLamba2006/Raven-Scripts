int inAirTicks;
boolean wasEnabled;

void onPreUpdate() {
    Entity player = client.getPlayer();
    Vec3 motion = client.getMotion();
    if (!modules.isEnabled("Scaffold") || !client.keybinds.isKeyDown(57)) {
        modules.setSlider("Scaffold", "Fast scaffold", 3);
        modules.setButton("Scaffold", "Fast on RMB", true);
        if (modules.isEnabled("Scaffold") && wasEnabled) {
            client.setMotion(0, motion.y, 0);
        }
        wasEnabled = false;
        return;
    }
    wasEnabled = true; 
    modules.setSlider("Scaffold", "Fast scaffold", 1);
    modules.setButton("Scaffold", "Fast on RMB", false);
    inAirTicks = player.onGround() ? 0 : inAirTicks + 1;
    if (player.onGround()) client.setMotion(motion.x, 0.4196, motion.z);
    if (inAirTicks == 3) client.setMotion(motion.x, 0, motion.z);
    if (inAirTicks == 4) client.setMotion(motion.x, 0, motion.z);
    if (inAirTicks == 5) client.setMotion(motion.x, 0.4191, motion.z);
    if (inAirTicks == 6) client.setMotion(motion.x, 0.3275, motion.z);
    if (inAirTicks == 11) client.setMotion(motion.x, -0.5, motion.z);
}