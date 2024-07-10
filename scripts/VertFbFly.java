boolean start, done, jump, stop;
int ticks, cancelTicks;
double boostticks, mode;
void onLoad() {
    modules.registerSlider("Vertical motion", 0.70, 0.10, 2, 0.1);
    modules.registerSlider("Slow motion", 0.14, 0.10, 0.30, 0.01);
}


void onPreUpdate() {
    Entity player = client.getPlayer();
    Vec3 motion = client.getMotion();
    double vert = modules.getSlider(scriptName, "Vertical motion");
    double slow = modules.getSlider(scriptName, "Slow motion");

    if (client.keybinds.isPressed("sneak") && ticks > 0) {
        stop = true;
    }

    if (ticks < 2) {
        client.keybinds.setPressed("left", false);
        client.keybinds.setPressed("right", false);
        client.keybinds.setPressed("forward", false);
        client.keybinds.setPressed("back", false);
    } else if (ticks == 2) {
        if (client.keybinds.isKeyDown(17)) {
            client.keybinds.setPressed("forward", true);
        }
        if (client.keybinds.isKeyDown(31)) {
            client.keybinds.setPressed("back", true);
        }
        if (client.keybinds.isKeyDown(30)) {
            client.keybinds.setPressed("left", true);
        }
        if (client.keybinds.isKeyDown(32)) {
            client.keybinds.setPressed("right", true);
        }
    }
    if (player.onGround() && ticks > 10) {
        modules.disable(scriptName);
    }
    
    cancelTicks++;
    if (cancelTicks <= 5) {
        client.setMotion(motion.x * slow,motion.y,motion.z * slow);
    }
    if (player.getHurtTime() >= 3 && !done) {
        start = true;
    }
    if (start) {
        ticks++;
    }
    if (!stop) {
        if (ticks < 20 && ticks > 0) {
            client.setMotion(motion.x,1.15,motion.z);
        } else if (ticks == 20) {
            client.setMotion(motion.x,1.1,motion.z);
        } else if (ticks == 21) {
            client.setMotion(motion.x,1,motion.z);
        } 
    }
    if (ticks >= 35) {
        client.setMotion(motion.x,motion.y + 0.0283,motion.z);
    }
        
    if (ticks >= 50) {
        done = true;
        start = false;
    }
}

void onDisable() {
    modules.enable("AntiFireball");
    modules.setSlider("Long Jump", "Boost ticks", boostticks);
    modules.setSlider("Long Jump", "Mode", mode);
    modules.setButton("Long Jump", "Jump", jump);
    start = false;
    done = false;
    stop = false;
    ticks = 0;
    cancelTicks = 0;
    modules.enable("AntiFireball");
}


void onEnable() {
    boostticks = modules.getSlider("Long Jump", "Boost ticks");
    mode = modules.getSlider("Long Jump", "Mode");
    jump = modules.getButton("Long Jump", "Jump");

    modules.setSlider("Long Jump", "Boost ticks", 0);
    modules.setSlider("Long Jump", "Mode", 1);
    modules.setButton("Long Jump", "Jump", true);
    
    modules.enable("Long Jump");

    if (!modules.isEnabled("Long Jump")) {
        modules.disable(scriptName);
    }
}
