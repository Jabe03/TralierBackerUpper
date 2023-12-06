package Online;

import android.util.Log;

/**
 * An implementation of the PacketProcessor class that the Client class uses to execute its packets
 * @author Joshua Bergthold
 */
public class ClientPacketProcessor extends PacketProcessor{
    Client host;
    /**
     * Creates a ClientPacketProcessor with the given Client as its host
     * @param host Client that this processor communicates with
     */
    public ClientPacketProcessor(Client host) {
        super(host);
        this.host = host;
    }


    @Override
    public void executePacket(Packet p){
        host.incrementPacketsReceived();
        if(p == null){
            return;
        }
        Log.d("PacketReceived", p.toJSONString());
        switch(p.getCommand().getType()){

            case DefaultOnlineCommands.SIMPLE_TEXT:
                if(!p.getAuthorID().equals(host.getID())){
                    System.out.println("Message from " + Packet.shortenedID(p.getAuthorID())+ ": " + p.getData());
                }
                System.out.println(p.getData());
                return;
            case DefaultOnlineCommands.INFO:
                Log.d("PacketReceived", "Made it to INFO");
                switch(p.getCommand().getCommandLine(1)){
                    case DefaultOnlineCommands.STEERING_ANGLE:
                        Log.d("PacketProcessing", "Received a inf;str; packet, updating");
                        //this.host.updateSuggestedSteeringAngle((double)p.getData());
                        break;
                    default:
                        Log.d("PacketProcessing", p.getCommand().getCommandLine(1));
                }

                return;
        }
        super.executePacket(p);

    }
}
