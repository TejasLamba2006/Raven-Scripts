void onLoad() {
    modules.registerButton("only consumables", true);
}

void onPreUpdate() {
    Entity player = client.getPlayer();
    if (player.isUsingItem()) {
        if (!modules.getButton(scriptName, "only consumables") || !player.getHeldItem().type.equalsIgnoreCase("itemsword")) {
            int slot = client.getSlot();
            client.sendPacket(new C09(slot < 8 ? slot + 1 : 0));
            client.sendPacket(new C09(slot));
        }
    }
}
