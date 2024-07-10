int tick, ticks;

void onEnable() {
    resetValues();
}

void onDisable() {
    resetValues();
}

void resetValues() {
    tick = ticks = 0;
}

void onPreMotion(PlayerState state) {
    if (client.keybinds.isKeyDown(57) && modules.isEnabled("Scaffold")) {
        double amount = Math.random() * 0.000000001;
        modules.setSlider("Scaffold", "Fast scaffold", 1);
        
        int stateY = (int) Math.round((state.y % 1) * 10000);
        Vec3 mot = client.getMotion();

        if (!state.onGround) {
            if (stateY == 4200) {
                tick = 1;
            } else if (tick > 0) {
                tick++;
            }
        } else {
            tick = 0;
        }

        if (stateY == 1661) {
            double diff = 0.04 + Math.random() / 1000d;
            client.setMotion(mot.x, mot.y - diff, mot.z);
            ticks = 1;
        } else if (ticks == 1) {
            client.setMotion(mot.x, mot.y - 1, mot.z);
            ticks = 2;
        } else if (ticks == 2 && state.y % 1 == 0) {
            ticks = 0;
            state.y += amount;
        }
    } else {
        modules.setSlider("Scaffold", "Fast scaffold", 3);
    }
}

