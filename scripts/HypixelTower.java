private int inAirTicks = 0;
Entity player = client.getPlayer();

void onPreMotion(PlayerState state) {
    if (player.onGround()) {
        modules.enable("HealthIndicatorV2");
        inAirTicks = 0;
        if (client.keybinds.isPressed("jump")) {
            client.setSpeed(0.375);
            client.setMotion(client.getMotion().x, 0.3775, client.getMotion().z);
        }
    }
}

void onDisable() {
    inAirTicks = 0;
    client.setMotion(0, client.getMotion().y, 0);
}

void onRenderTick(float partialTicks) {
    int[] size = client.getDisplaySize();
    float x = size[0] / 2f;
    float y = size[1] / 2f;
    if (client.keybinds.isPressed("jump")) {
        client.render.text("\u00A7a Towering...",  x - 30, y + 15, 1, -1, true);
        if (modules.isEnabled("HealthIndicatorV2")) {
            modules.disable("HealthIndicatorV2");
        }
    }
}