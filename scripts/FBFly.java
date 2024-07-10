boolean start = false, done = false;
int ticks = 0;

void onLoad() {
    modules.registerSlider("Motion-Y", 0.35, 0.01, 0.4, 0.01);
}

void onPreUpdate() {
    Entity player = client.getPlayer();
    Vec3 motion = client.getMotion();
    double motionY = modules.getSlider(scriptName, "Motion-Y");

    if (player.getHurtTime() >= 3) {
        start = true;
    }
    
    if (start) {
        ticks++;
    }
    
    if (ticks > 0 && ticks < 30) {
        client.setMotion(motion.x, motionY, motion.z);
    } else if (ticks >= 30) {
        done = true;
        start = false;
    }
    
    if (player.getHurtTime() == 0 && done) {
        modules.disable(scriptName);
        reset();
    }
}

void onDisable() {
    reset();
}

void onEnable() {
    modules.enable("Long Jump");

    if (!modules.isEnabled("Long Jump")) {
        modules.disable(scriptName);
    }
}

void reset() {
    start = false;
    done = false;
    ticks = 0;
}
