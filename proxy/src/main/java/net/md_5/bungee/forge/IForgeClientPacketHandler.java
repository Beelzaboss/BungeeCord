package net.md_5.bungee.forge;

import net.md_5.bungee.UserConnection;
import net.md_5.bungee.protocol.packet.PluginMessage;

public interface IForgeClientPacketHandler<S>
{
    /**
     * Handles any {@link PluginMessage} packets.
     * 
     * @param message The {@link PluginMessage} to handle.
     * @param con The {@link UserConnection} to send packets to or read from.
     * @return The state to transition to.
     */
    public S handle(PluginMessage message, UserConnection con);
    
    /**
     * Sends any {@link PluginMessage} packets.
     * 
     * @param message The {@link PluginMessage} to send.
     * @param con The {@link UserConnection} to send packets to or read from.
     * @return The state to transition to.
     */
    public S send(PluginMessage message, UserConnection con);
}