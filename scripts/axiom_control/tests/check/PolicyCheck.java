package check;
import dev.apocalypse.axiomcontrol.AxiomControl;
import com.moulberry.axiom.AxiomServer;
import com.moulberry.axiom.restrictions.AxiomPermission;
import com.moulberry.axiom.hooks.MinecraftServerExt;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.ResourceLocation;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Mod(value="axiom_policy_check",dist=Dist.DEDICATED_SERVER)
public class PolicyCheck {
    public static int PASSED_DISPATCH=0;
    private int checks=0;
    public PolicyCheck(){NeoForge.EVENT_BUS.addListener(this::started);}
    private void verify(boolean ok,String why){if(!ok)throw new AssertionError(why);checks++;}
    private void started(ServerStartedEvent event){
        var server=event.getServer();
        try{
            verify(server.isDedicatedServer(),"Dedicated runtime");
            verify(!AxiomControl.allowed(server),"Default disabled at startup");
            var profile=new GameProfile(UUID.fromString("00000000-0000-0000-0000-000000000077"),"AxiomControlTest");
            var player=new ServerPlayer(server,server.overworld(),profile,ClientInformation.createDefault());
            var listener=new ServerGamePacketListenerImpl(server,new Connection(PacketFlow.SERVERBOUND),player,CommonListenerCookie.createInitial(profile,false));
            var backend=((MinecraftServerExt)server).axiom$getAxiomServer();
            var ids=new BufferedReader(new InputStreamReader(PolicyCheck.class.getResourceAsStream("/axiom-packet-ids.txt"))).lines().toList();
            var commands=server.getCommands().getDispatcher();var source=server.createCommandSourceStack();
            verify(commands.execute("axiomserver status",source)==1,"Status command");
            try {commands.execute("axiomserver enable",source.withPermission(0));throw new AssertionError("Non-op enabled Axiom");}
            catch(com.mojang.brigadier.exceptions.CommandSyntaxException expected){checks++;}
            for(boolean operator:new boolean[]{false,true}){
                if(operator)server.getPlayerList().op(profile);
                backend.activeAxiomPlayers.add(profile.getId());
                verify(!AxiomServer.hasPermission(player,AxiomPermission.USE),"Native permission denied while off including op");
                verify(!AxiomServer.canUseAxiom(player),"Native active player denied");
                verify(!AxiomServer.canUseAxiom(player,AxiomPermission.USE),"Native permission overload denied");
                verify(!AxiomServer.acceptHandshake(player,1L),"Handshake denied while off");
                for(String id:ids){
                    long before=AxiomControl.blockedPayloads();int seen=PASSED_DISPATCH;
                    listener.handleCustomPayload(new ServerboundCustomPayloadPacket(new DiscardedPayload(ResourceLocation.parse(id))));
                    verify(AxiomControl.blockedPayloads()==before+1,"Blocked packet "+id);
                    verify(PASSED_DISPATCH==seen,"No downstream dispatch "+id);
                }
            }
            verify(commands.execute("axiomserver enable",source)==1,"Enable command");
            verify(AxiomControl.allowed(server),"Enabled runtime state");
            verify(AxiomServer.hasPermission(player,AxiomPermission.USE),"Native op permission restored");
            verify(AxiomServer.canUseAxiom(player),"Native active access restored");
            server.getPlayerList().deop(profile);
            verify(!AxiomServer.hasPermission(player,AxiomPermission.USE),"Enable preserves native non-op denial");
            for(String id:ids){
                long before=AxiomControl.blockedPayloads();int seen=PASSED_DISPATCH;
                listener.handleCustomPayload(new ServerboundCustomPayloadPacket(new DiscardedPayload(ResourceLocation.parse(id))));
                verify(AxiomControl.blockedPayloads()==before,"Enabled payload not blocked");
                verify(PASSED_DISPATCH==seen+1,"Enabled dispatch allowed");
            }
            verify(commands.execute("axiomserver disable",source)==1,"Disable command");
            verify(!AxiomControl.allowed(server),"Immediate disable");
            for(String id:new String[]{"minecraft:brand","neoforge:register","kubejs:test","bettercombat:test"}){
                long before=AxiomControl.blockedPayloads();int seen=PASSED_DISPATCH;
                listener.handleCustomPayload(new ServerboundCustomPayloadPacket(new DiscardedPayload(ResourceLocation.parse(id))));
                verify(AxiomControl.blockedPayloads()==before,"Unrelated protocol preserved");
                verify(PASSED_DISPATCH==seen+1,"Unrelated dispatch fall-through");
            }
            commands.execute("axiomserver enable",source);
            System.out.println("AXIOM_POLICY_CHECK PASS checks="+checks+"; official packet IDs="+ids.size()+"; default-off, op/non-op, permissioned commands, native authorization, enabled dispatch, disable immediate");
        }catch(Throwable t){System.out.println("AXIOM_POLICY_CHECK FAIL");t.printStackTrace();}
        server.halt(false);
    }
}
