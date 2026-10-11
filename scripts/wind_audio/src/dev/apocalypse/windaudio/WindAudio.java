package dev.apocalypse.windaudio;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent;
import weather2.ClientTickHandler;
import weather2.SoundRegistry;
import weather2.config.ConfigMisc;
import weather2.config.ConfigWind;
import weather2.util.WindReader;

@Mod(value=WindAudio.ID, dist=Dist.CLIENT)
public final class WindAudio {
    public static final String ID="apocalypse_wind_audio";
    private static final ModConfigSpec.Builder B=new ModConfigSpec.Builder();
    private static final ModConfigSpec.BooleanValue ENABLED=B.define("enabled",true);
    private static final ModConfigSpec.DoubleValue MAX=B.comment("Maximum added wind volume; also follows Minecraft Weather and Master sliders.").defineInRange("maximumVolume",0.65,0,1);
    private static final ModConfigSpec.DoubleValue ONSET=B.comment("Weather2 relative wind units, not metres per second.").defineInRange("audibleAbove",0.08,0,10);
    private static final ModConfigSpec.DoubleValue FULL=B.comment("Speed reaching maximum volume. Must exceed audibleAbove.").defineInRange("fullVolumeAt",1.2,0.01,20);
    private static final ModConfigSpec.DoubleValue SHELTER=B.comment("Volume multiplier beneath a solid vanilla-world roof. Moving Sable roofs need gameplay validation.").defineInRange("shelteredMultiplier",0.2,0,1);
    private static final ModConfigSpec.DoubleValue WATER=B.defineInRange("underwaterMultiplier",0.05,0,1);
    private static final ModConfigSpec.DoubleValue RISE=B.defineInRange("riseSeconds",1.0,0.05,10);
    private static final ModConfigSpec.DoubleValue FALL=B.defineInRange("fallSeconds",2.0,0.05,10);
    private static final ModConfigSpec SPEC=B.build();
    private ClientLevel world;
    private WindLoop loop;
    private float target;
    private float volume;
    private int ticks;
    private int nextStart;
    public WindAudio(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT,SPEC,ID+"-client.toml");
        NeoForge.EVENT_BUS.addListener(this::tick);
        bus.addListener(this::reload);
    }
    private void reload(SoundEngineLoadEvent event) {
        Minecraft.getInstance().execute(this::reset);
    }
    private void reset() {
        if(loop!=null) {loop.finish();Minecraft.getInstance().getSoundManager().stop(loop);}
        loop=null;world=null;target=volume=0;ticks=nextStart=0;
    }
    private void tick(ClientTickEvent.Post event) {
        Minecraft mc=Minecraft.getInstance();
        if(mc.level==null || mc.player==null) {reset();return;}
        if(world!=mc.level) {reset();world=mc.level;}
        if(mc.isPaused()) return;
        ticks++;
        if(ticks%5==1) {
            boolean dimension=Arrays.stream(ConfigMisc.Dimension_List_WindEffects.split(",")).map(String::trim).anyMatch(mc.level.dimension().location().toString()::equals);
            boolean ready=ClientTickHandler.weatherManager!=null && ClientTickHandler.lastWorld==mc.level;
            if(!ENABLED.get() || !ConfigWind.Misc_windOn || !dimension || !ready) target=0;
            else {
                BlockPos eye=BlockPos.containing(mc.player.getEyePosition());
                double exposure=mc.player.isUnderWater()?WATER.get():mc.level.canSeeSky(eye)?1:SHELTER.get();
                target=WindEnvelope.target(WindReader.getWindSpeed(mc.level,eye),ONSET.get(),FULL.get(),MAX.get(),exposure);
            }
        }
        volume=WindEnvelope.advance(volume,target,RISE.get(),FALL.get());
        if(volume==0 && target==0) {
            if(loop!=null) {loop.finish();mc.getSoundManager().stop(loop);loop=null;}
            return;
        }
        // Retry at most every two seconds after a reload, muted startup or stolen audio channel.
        if(ticks>=nextStart && (loop==null || !mc.getSoundManager().isActive(loop))) {
            if(loop!=null) {loop.finish();mc.getSoundManager().stop(loop);}
            loop=new WindLoop(SoundRegistry.get("env.wind_calm"));
            loop.setVolume(volume);mc.getSoundManager().play(loop);nextStart=ticks+40;
        }
        if(loop!=null) loop.setVolume(volume);
    }
    private static final class WindLoop extends AbstractTickableSoundInstance {
        WindLoop(SoundEvent event) {
            super(event,SoundSource.WEATHER,RandomSource.create());
            looping=true;delay=0;relative=true;attenuation=SoundInstance.Attenuation.NONE;volume=0;pitch=1;
        }
        public boolean canStartSilent() {return true;}
        void setVolume(float value) {volume=value;}
        void finish() {stop();}
        public void tick() {
            Minecraft mc=Minecraft.getInstance();
            if(mc.level==null || mc.player==null) stop();
        }
    }
}
