List<CPacket> packetQueue = new ArrayList<>();

boolean onPacketSent(CPacket p) {
    if (p.name.equals("C16PacketClientStatus") || p instanceof C0E) {
        packetQueue.add(p);
        return false;
    }
    else if (p.name.equals("C0DPacketCloseWindow") && !packetQueue.isEmpty()) {
        for (CPacket packet : packetQueue) {
            client.sendPacketNoEvent(packet);
        }
        packetQueue.clear();
    }
    return true;
}

void onDisable() {
    packetQueue.clear();
}