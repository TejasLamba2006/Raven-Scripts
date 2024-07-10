boolean start, done;
int ticks;

void onPreUpdate() {
    Entity player = client.getPlayer();
    Vec3 motion = client.getMotion();
    if (player.getHurtTime() >= 3) {
        start = true;
    }
    if (start) {
        ticks++;
    }
    if (ticks < 25 && ticks != 0) {
        client.setMotion(motion.x,0.7,motion.z);
        client.print("flying ticks " + ticks);
    } else if (ticks >= 25) {
        done = true;
        start = false;
    }
    if (player.getHurtTime() == 0 && done) {
        modules.enable("AntiFireball");
        modules.disable("Test");
        start = false;
        done = false;
        ticks = 0;
    }
}
void onDisable() {
    modules.enable("AntiFireball");
    start = false;
    done = false;
    ticks = 0;
}

boolean fireball() {
    Entity player = client.getPlayer();
    return player.getHeldItem() != null && player.getHeldItem().type.contains("Fire");
}