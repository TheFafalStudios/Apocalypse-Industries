import dev.ashok.sablewaves.WavesConfig;
import dev.ashok.sablewaves.hotfix.ChunkSafety;
import dev.ashok.sablewaves.hotfix.BiomeSafety;
import dev.ashok.sablewaves.ocean.FetchSampler;
import dev.ashok.sablewaves.ocean.ShoreSampler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
public class TerrainTests {
    static int checks;
    static void check(boolean ok,String name){checks++;if(!ok)throw new AssertionError(name);}
    static boolean water(BlockPos p){return p.getY()<=63&&p.getY()>50;}
    public static void main(String[] args){
        WavesConfig config=new WavesConfig();
        if(args.length>0){
            ServerLevel one=new ServerLevel();one.fill(0,0,TerrainTests::water);
            FetchSampler.sample(one,4,63,4,1,0,config);
            check(one.missingRequests>0,"v3 should expose a missing chunk request");
            ShoreSampler.clear();
            ServerLevel sea=new ServerLevel();sea.fill(-5,5,TerrainTests::water);
            ServerLevel coast=new ServerLevel();coast.fill(-5,5,p->water(p)&&p.getX()<12);
            var a=ShoreSampler.sample(sea,4,63,4,40);
            var b=ShoreSampler.sample(coast,4,63,4,40);
            check(a==b,"v3 should incorrectly share cached sounding between worlds");
            System.out.println("Confirmed v3 regressions: missing chunk requests="+one.missingRequests+", cross-world cache reuse=true");
            return;
        }
        ServerLevel missing=new ServerLevel();
        check(!ChunkSafety.loaded(missing,-1,-1),"missing chunk");
        check(!ChunkSafety.fluid(missing,new BlockPos(-1,63,-1)).water(),"missing returns empty");
        check(missing.blockingCalls==0,"no blocking fluid read");
        ServerLevel sea=new ServerLevel();sea.fill(-5,5,TerrainTests::water);
        check(ChunkSafety.loaded(sea,-1,-1),"negative coordinates loaded");
        check(ChunkSafety.fluid(sea,new BlockPos(-1,63,-1)).water(),"loaded water");
        check(!ChunkSafety.fluid(sea,new BlockPos(0,50,0)).water(),"loaded dry");
        check(BiomeSafety.loadedNeighborhood(sea,new BlockPos(-1,63,-1)),"biome neighborhood");
        check(!BiomeSafety.loadedNeighborhood(missing,new BlockPos(0,63,0)),"missing biome denied");
        check(ChunkSafety.fluid(new Level(),new BlockPos(0,63,0)).water(),"client delegate");
        check(FetchSampler.sample(sea,4,63,4,0,0,config)==1,"calm wind");
        check(FetchSampler.sample(sea,4,63,4,1,0,config)==1,"unknown fetch is not coast");
        ServerLevel coast=new ServerLevel();coast.fill(-5,5,p->water(p)&&p.getX()<12);
        double fetch=FetchSampler.sample(coast,4,63,4,-1,0,config);
        check(Math.abs(fetch-(0.25+0.75*Math.sqrt(12.0/320)))<1e-12,"known coast fetch");
        ShoreSampler.clear();
        var open=ShoreSampler.sample(sea,4,63,4,40);
        var shore=ShoreSampler.sample(coast,4,63,4,40);
        check(!open.found,"open sea");check(shore.found&&shore.distance==8,"known shore");
        check(open!=shore,"world-local cache");check(open.waterDepth==13,"depth");
        check(ShoreSampler.sample(coast,4,63,4,40)==shore,"cache hit");
        check(ShoreSampler.sample(coast,4,63,4,8)!=shore,"radius included in key");
        check(ShoreSampler.sample(coast,4,64,4,40)!=shore,"height included in key");
        ServerLevel edge=new ServerLevel();edge.fill(0,0,TerrainTests::water);
        var edgeShore=ShoreSampler.sample(edge,4,63,4,60);
        check(!edgeShore.found,"unloaded edge is not shoreline");
        check(edge.blockingCalls==0&&edge.missingRequests==0,"no chunk requests at edge");
        ShoreSampler.clear();check(ShoreSampler.sample(coast,4,63,4,40)!=shore,"clear all caches");
        check(sea.blockingCalls==0&&coast.blockingCalls==0,"all production sampling is nonblocking");
        System.out.println("Terrain regression checks="+checks+"; blocking world reads=0; missing requests=0");
    }
}
