package net.md_5.bungee.forge;

import java.util.Timer;
import java.util.TimerTask;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.packet.PluginMessage;

/**
 * Handles the server part of the client handshake.
 */
@RequiredArgsConstructor
public class ForgeServerHandshake extends AbstractPacketHandler
{

    private final UserConnection con;
    private final ChannelWrapper ch;

    @Getter
    private final ServerInfo serverInfo;
    private final ProxyServer bungee;

    private PluginMessage modList = null;

    private ForgeServerHandshakeState state = ForgeServerHandshakeState.START;

    private PluginMessage clientModList = null;

    /**
     * Handles any {@link PluginMessage} that contains a Forge Handshake.
     *
     * @param message The message to handle.
     * @throws IllegalArgumentException If the wrong packet is sent down.
     */
    public void handle(PluginMessage message) throws IllegalArgumentException
    {
        if ( !message.getTag().equalsIgnoreCase( ForgeConstants.forgeTag ) ) {
            throw new IllegalArgumentException( "Expecting a Forge Handshake packet." );
        }

        state = state.handle( message, con, ch );

        if ( state == ForgeServerHandshakeState.PENDINGUSER ) {
            // We are waiting for the user.
            setDelayedResponse();
        }
    }

    /**
     * Returns whether the server handshake has been initiated.
     *
     * @return <code>true</code> if the server has started a Forge handshake.
     */
    public boolean isServerForge() {
        return state != ForgeServerHandshakeState.START;
    }

    /**
     * Sends the defined plugin message.
     *
     * @param message The {@link PluginMessage} to send.
     */
    private void send(PluginMessage message) {
        state = state.send( message, con, ch );
    }

    /**
     * Sets the delayed response, which waits for the mod list to load.
     */
    private void setDelayedResponse()
    {
        // If we cannot identify them as a forge user, then wait a couple of seconds, as we might be waiting for the 
        // user to complete the forge handshake. 
        final Timer timer = new Timer();

        // Setup the success callback
        con.setDelayedPacketSender( new IForgePacketSender() {
            @Override
            public void send(PluginMessage message) {
                timer.cancel();
                this.send( message );
            }
        } );

        // If we don't get a mod list in a reasonable amount of time, then we should 
        // kick the client.
        timer.schedule( new TimerTask() {
            @Override
            public void run() {
                if ( timer != null ) {
                    timer.cancel();
                }

                // If this wasn't cancelled, then continue anyway.
                if ( con.isForgeUser() ) {
                    return;
                }

                try {
                    // If the user is not a mod user, then throw them off.
                    con.getPendingConnects().remove( serverInfo );
                    ch.close();

                    String message = bungee.getTranslation( "connect_kick" ) + serverInfo.getName() + ": " + bungee.getTranslation( "connect_kick_modded" );
                    if ( con.getServer() == null ) {
                        con.disconnect( message );
                    } else {
                        con.sendMessage( ChatColor.RED + message );
                    }
                } finally {
                }
            }
        }, 2000 );
    }
}
