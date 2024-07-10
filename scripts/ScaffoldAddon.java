boolean speed;
int inAirTicks;
boolean CheckGround;

void onLoad() {
    modules.registerButton("Lowhop", false);
}


void onPreUpdate() {
    if (client.isKeyDown(57) && client.keybinds.isPressed("forward") && modules.isEnabled("Scaffold") && client.getPlayer().onGround()) {
        client.keybinds.setPressed("use", false);
        client.keybinds.setPressed("jump", false);
        if (!isDiagonal() && !(client.keybinds.isPressed("right") || client.keybinds.isPressed("left"))) {
                client.setSprinting(true);
                client.jump();
        } else if (isDiagonal() || (client.keybinds.isPressed("right") || client.keybinds.isPressed("left"))) {
                client.jump();
        }
        speed = true;
    } else if (modules.isEnabled("Scaffold") && !client.isMouseDown(1) && !client.isKeyDown(57) && speed) {
        client.setMotion(0, client.getMotion().y, 0);
        speed = false;
    }
    if (modules.getButton("Scaffoldaddon","Lowhop"))
    if (client.isMouseDown(0) && modules.isEnabled("Scaffold")) {
        Entity player = client.getPlayer();
        inAirTicks = player.onGround() ? 0 : inAirTicks + 1;
        Vec3 motion = client.getMotion();
        if (player.onGround()) {
            client.setMotion(motion.x, 0.4191, motion.z);
            CheckGround = true;
            speed = true;
        }
        else if  (modules.isEnabled("Scaffold") && !client.isMouseDown(0) && !client.isKeyDown(57)) {
        }
        if (inAirTicks == 1 && CheckGround) client.setMotion(motion.x, 0.327318, motion.z);
    if (inAirTicks == 5 && CheckGround) client.setMotion(motion.x, -0.005, motion.z);
    if (inAirTicks == 6 && CheckGround) {
        client.setMotion(motion.x, -1.0, motion.z);
        CheckGround = false;
    }
}
}


boolean isDiagonal() {
    float yaw = client.getPlayer().getYaw();
    yaw = (yaw + 360) % 360;
    final float threshold = 9;
    boolean isNorth = Math.abs(yaw - 0) < threshold || Math.abs(yaw - 360) < threshold;
    boolean isSouth = Math.abs(yaw - 180) < threshold;
    boolean isEast = Math.abs(yaw - 90) < threshold;
    boolean isWest = Math.abs(yaw - 270) < threshold;
    return !(isNorth || isSouth || isEast || isWest);
}