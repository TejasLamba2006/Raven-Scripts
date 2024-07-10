int offGroundTicks = 0;

void onPreMotion(PlayerState state) {
    Entity player = client.getPlayer();
    double posX =  player.getPosition().x;
    double posY =  player.getPosition().y;
    double posZ =  player.getPosition().z;
    if (player.onGround()) {
        offGroundTicks = 0;
    } else {
        offGroundTicks++;
    }
    if (modules.isEnabled("Scaffold") && client.keybinds.isPressed("jump")) {
        if (posY % 1 <= 0.00153598) {
            player.setPosition(new Vec3(posX, Math.floor(posY), posZ));
            client.setMotion(client.getMotion().x, 0.42f, client.getMotion().z);
        } else if (posY % 1 < 0.1 && offGroundTicks != 0) {
            client.setMotion(client.getMotion().x, 0, client.getMotion().z);
            player.setPosition(new Vec3(posX, Math.floor(posY), posZ));
        }
    }
}