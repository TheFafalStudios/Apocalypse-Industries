import dev.apocalypse.windaudio.WindEnvelope;
import java.lang.reflect.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.client.resources.sounds.SoundInstance;
public class WindAudioCheck {
 static int checks=0;
 static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception {
  check(WindEnvelope.target(0,.08,1.2,.65,1)==0,"calm silent");
  check(WindEnvelope.target(.08,.08,1.2,.65,1)==0,"threshold silent");
  check(WindEnvelope.target(Double.NaN,.08,1.2,.65,1)==0,"invalid wind silent");
  check(WindEnvelope.target(Double.POSITIVE_INFINITY,.08,1.2,.65,1)==0,"infinite wind silent");
  float previous=0;
  for(int i=0;i<=2000;i++){
   float v=WindEnvelope.target(i/1000.0,.08,1.2,.65,1);
   check(v>=previous && v>=0 && v<=.65001,"monotonic bounded response");previous=v;
  }
  check(Math.abs(WindEnvelope.target(.7,.08,1.2,.65,.2)/WindEnvelope.target(.7,.08,1.2,.65,1)-.2)<.0001,"roof attenuation");
  check(WindEnvelope.target(.7,.08,1.2,.65,.05)<WindEnvelope.target(.7,.08,1.2,.65,.2),"underwater quieter");
  check(WindEnvelope.target(5,1,0,.65,1)<=.65f,"misordered thresholds bounded");
  float v=0;
  for(int i=0;i<20;i++){float n=WindEnvelope.advance(v,.65f,1,2);check(n>=v&&n<=.65f,"fade up no overshoot");v=n;}
  check(v>.4f&&v<.42f,"one-second attack");
  for(int i=0;i<500;i++){float n=WindEnvelope.advance(v,0,1,2);check(n<=v&&n>=0,"fade down no undershoot");v=n;}
  check(v==0,"eventual silence releases loop");
  // Load and inspect the production classes against the actual mapped client APIs.
  Class<?> mod=Class.forName("dev.apocalypse.windaudio.WindAudio",false,WindAudioCheck.class.getClassLoader());
  check(mod.getDeclaredConstructors().length==1,"mod constructor links");
  check(mod.getDeclaredMethods().length>0,"event handler signatures link");
  Class<?> loop=Class.forName("dev.apocalypse.windaudio.WindAudio$WindLoop");
  Constructor<?> ctor=loop.getDeclaredConstructor(SoundEvent.class);ctor.setAccessible(true);
  SoundEvent sound=SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("weather2","env.wind_calm"));
  SoundInstance instance=(SoundInstance)ctor.newInstance(sound);
  check(instance.isLooping(),"loop enabled");check(instance.isRelative(),"listener relative");
  check(instance.getAttenuation()==SoundInstance.Attenuation.NONE,"no distance fade");
  check(instance.getSource()==SoundSource.WEATHER,"weather slider controls sound");
  check(instance.canStartSilent(),"silent-start permitted");
  Field asset=net.minecraft.client.resources.sounds.AbstractSoundInstance.class.getDeclaredField("sound");asset.setAccessible(true);
  asset.set(instance,new net.minecraft.client.resources.sounds.Sound(ResourceLocation.fromNamespaceAndPath("weather2","env/wind_calm"),random -> 1.0f,random -> 1.0f,1,net.minecraft.client.resources.sounds.Sound.Type.FILE,false,false,16));
  Method set=loop.getDeclaredMethod("setVolume",float.class);set.setAccessible(true);set.invoke(instance,.3f);
  check(Math.abs(instance.getVolume()-.3f)<.0001,"native volume setter");
  Method finish=loop.getDeclaredMethod("finish");finish.setAccessible(true);finish.invoke(instance);
  check(((net.minecraft.client.resources.sounds.TickableSoundInstance)instance).isStopped(),"native stop flag");
  System.out.println("PASS "+checks+" checks: audio response, fades and real Minecraft sound-instance linkage. No live audio/gameplay test.");
 }
}

