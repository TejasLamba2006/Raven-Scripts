boolean fireball;
boolean velocity;

void onPreUpdate() {
    List<Entity> entities = client.getWorld().getEntities();
    Entity player = client.getPlayer();
    if(velocity) {
        modules.enable("Velocity");
        if(player.onGround()) {
            modules.setSlider("Velocity", "Vertical", 100);
            modules.setSlider("Velocity", "Horizontal", 0);
        } else {
            modules.setSlider("Velocity", "Vertical", 0);
            modules.setSlider("Velocity", "Horizontal", 0);
        }
    }
    for (Entity entity : entities) {
        if (!entity.getName().equals("Fireball")) {
            fireball = false;
            velocity = true;
        } else {
            modules.disable("Velocity");
            velocity = false;
            return;
        }

    }

}


