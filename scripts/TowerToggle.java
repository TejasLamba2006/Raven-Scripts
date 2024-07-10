// 0 = vanilla tower
// 1 = vertical tower
final int DEFAULT_STATE = 0;

// mouse button to press
// 0 = left click
// 1 = right click
// 2 = middle click
final int HOLD_BUTTON = 1;

// print toggle tower message
final boolean PRINT_TOGGLE = false;

boolean pressed;

void onDisable() {
    pressed = false;
    if (DEFAULT_STATE == 0) {
        vanillaTower();
    } else {
        verticalTower();
    }
}

void onPreUpdate() {
    if (!modules.isEnabled("Scaffold") || !modules.getButton("Scaffold", "Tower")) {
        return;
    }
    if (!client.keybinds.isPressed("jump")) {
        return;
    }
    boolean _pressed = client.keybinds.isMouseDown(HOLD_BUTTON);
    if (_pressed) {
        if (!pressed) {
            pressed = true;
            if (DEFAULT_STATE == 0) {
                verticalTower();
            } else {
                vanillaTower();
            }
        }
    } else if (pressed) {
        pressed = false;
        if (DEFAULT_STATE == 0) {
            vanillaTower();
        } else {
            verticalTower();
        }
    }
}

void verticalTower() {
    modules.setButton("Scaffold", "Multi-place", true);
    modules.setSlider("Scaffold", "Rotation", 1);
    modules.setSlider("Tower", "Mode", 0);
    modules.setSlider("Tower", "Ground ticks", 1);
    modules.setSlider("Tower", "Speed", 2.3);
    modules.setSlider("Tower", "Diagonal speed", 2.2);
    modules.setSlider("Tower", "Slowed speed", 0);
    modules.setSlider("Tower", "Slowed ticks", 3);
    if (PRINT_TOGGLE) {
        print("&7toggled tower: &bvertical");
    }
}

void vanillaTower() {
    modules.setButton("Scaffold", "Multi-place", true);
    modules.setSlider("Scaffold", "Rotation", 1);
    modules.setSlider("Tower", "Mode", 0);
    modules.setSlider("Tower", "Ground ticks", 0);
    modules.setSlider("Tower", "Speed", 5);
    modules.setSlider("Tower", "Diagonal speed", 2.4);
    modules.setSlider("Tower", "Slowed speed", 0);
    modules.setSlider("Tower", "Slowed ticks", 5);
    if (PRINT_TOGGLE) {
        print("&7toggled tower: &bvanilla");
    }
}

void print(Object obj) {
    client.print("&7[&dS&7] &r" + obj);
}