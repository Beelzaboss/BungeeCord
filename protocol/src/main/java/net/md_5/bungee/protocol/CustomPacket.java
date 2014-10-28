package net.md_5.bungee.protocol;

public abstract class CustomPacket extends DefinedPacket
{

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
