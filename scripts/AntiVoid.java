boolean falling = false, air = false, killaura = false, rcing = false;
int jumpticks = 0;

void onPreUpdate() {
    Entity player = client.getPlayer();
    double pitch = player.getPitch();
    int dist = fallDistance();
    int blinkDist = 9;

    if (jumpticks-- <= 0 && !client.isFlying() && dist == -1 && !falling && !player.onGround() && modules.getKillAuraTarget() == null) {
        falling = true;
        killaura = modules.isEnabled("KillAura");
        modules.disable("KillAura");
        modules.enable("Blink");
    } else if (falling && player.getFallDistance() > blinkDist && dist == -1 && !air) {
        Vec3 pos = player.getPosition();
        air = true;
        client.sendPacketNoEvent(new C03(new Vec3(pos.x, -420, pos.z), false));
        modules.disable("Blink");
    } else if (falling && (player.onGround() || dist != -1 || modules.getKillAuraTarget() != null)) {
        if (killaura) modules.enable("KillAura");
        falling = air = killaura = false;
        modules.disable("Blink");
    }
}

void onPostPlayerInput() {
    rcing = client.keybinds.isPressed("use");
    if (client.keybinds.isPressed("jump")) jumpticks = 0;
}

int fallDistance() {
    int fallDist = -1;
    Vec3 pos = client.getPlayer().getPosition();
    int y = (int) Math.floor(pos.y) - 1;
    for (int i = y; i > -1; i--) {
        Block block = client.getWorld().getBlockAt((int) Math.floor(pos.x), i, (int) Math.floor(pos.z));
        if (block.name.equals("air") || block.name.contains("sign")) continue;
        fallDist = y - i;
        break;
    }
    return fallDist;
}