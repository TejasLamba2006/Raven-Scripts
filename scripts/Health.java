void onRenderTick(float partialTicks) {
    if (!client.getScreen().isEmpty()) {
        return;
    }

    Entity closestEntity = null;
    double closestDistanceSq = Double.MAX_VALUE;
    Entity player = client.getPlayer();
    for (Entity entity : client.getWorld().getEntities()) {
        if (entity == player) {
            continue;
        }
        double distanceSq = entity.getPosition().distanceToSq(player.getPosition());
        if (distanceSq < closestDistanceSq) {
            closestEntity = entity;
            closestDistanceSq = distanceSq;
        }
    }

    // heart icon 
    if (closestEntity != null) {
        float health = closestEntity.getHealth();
        int hearts = (int) Math.ceil(health / 2); 
        hearts = Math.min(hearts, 10); 
        String healthText = hearts + " ";
        healthText += "\u2764"; 
        int[] size = client.getDisplaySize();
        int x = size[0] / 2;
        int y = size[1] / 2 + 20; 
        int textColor = Color.white.getRGB();

      
        if (hearts <= 3) {
            textColor = Color.red.getRGB(); 
        } else if (hearts <= 7) {
            textColor = Color.yellow.getRGB(); 
        } else {
            textColor = Color.green.getRGB();
        }
        client.render.text(healthText, x, y, textColor, true);
    }
}
