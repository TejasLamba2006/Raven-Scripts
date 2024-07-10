Entity player = client.getPlayer();

void onRenderTick(float partialTicks) {
    int[] size = client.getDisplaySize();
    float x = size[0] / 2f;
    float y = size[1] / 2f;
    String heart = "\u2764"; //heart symbole
    float health = Math.round(player.getHealth() / 2 * 2) / 2f;
    if (player.isDead()) health = 10;

	String color = (health >= 10) ? "\u00A7a" : (health >= 8) ? "\u00A7e" : (health >= 6) ? "\u00A76" : (health >= 4) ? "\u00A7c" : "\u00A74";
    client.render.text(color + health + " " + heart, x - 15, y + 5, 1, -1, true);
}