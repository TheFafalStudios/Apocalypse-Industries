package dev.apocalypse.submarinelava;
import com.maxenonyme.vortexengine.VortexConfig;
import com.maxenonyme.vortexengine.light.*;
import com.maxenonyme.vortexengine.mist.MistField;
import dev.apocalypse.submarinelava.mixin.*;
import net.minecraft.client.Minecraft;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import net.irisshaders.iris.gl.texture.TextureType;
import org.lwjgl.opengl.*;
public final class VortexSamplers {
 private static int black;
 private static int black(){
  if(black==0){black=GL45.glCreateTextures(GL12.GL_TEXTURE_3D);GL45.glTextureStorage3D(black,1,GL30.GL_RGBA16F,1,1,1);
   GL44.glClearTexImage(black,0,GL11.GL_RGBA,GL11.GL_FLOAT,new float[4]);
   GL45.glTextureParameteri(black,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL45.glTextureParameteri(black,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);}
  return black;
 }
 public static void register(ProgramSamplers.Builder builder,int program){
  if(!builder.hasSampler("aiVortexField"))return;
  builder.addDynamicSampler(TextureType.TEXTURE_2D,()->{
   if(com.maxenonyme.createsubmarine.submarine.client.SubmarineFogHandler.shouldFog()||LavaVisuals.active){int id=HullAccess.ai$hullDepth();if(id!=0)return id;}
   return HullAccess.ai$outside().getId();
  },null,"aiVortexHull");
  int grid=GL20.glGetUniformLocation(program,"aiVortexGrid"),tuning=GL20.glGetUniformLocation(program,"aiVortexTuning");
  int count=GL20.glGetUniformLocation(program,"aiMistCount"),positions=GL20.glGetUniformLocation(program,"aiMistPosition[0]"),colors=GL20.glGetUniformLocation(program,"aiMistColor[0]");
  builder.addDynamicSampler(TextureType.TEXTURE_3D,()->{
   boolean active=VortexBridge.active();boolean ready=active&&VortexBridge.lights()&&LightField.ready();
   var camera=Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
   if(grid>=0)GL41.glProgramUniform4f(program,grid,ready?(float)(camera.x-VortexFieldAccess.ai$x()):0,
    ready?(float)(camera.y-VortexFieldAccess.ai$y()):0,ready?(float)(camera.z-VortexFieldAccess.ai$z()):0,ready?VortexFieldAccess.ai$mode():0);
   int size=0;float[] p=new float[64],c=new float[64];
   if(active&&VortexBridge.mist()){
    var volumes=new java.util.ArrayList<>(MistField.volumes());
    volumes.sort(java.util.Comparator.comparingDouble(v->camera.distanceToSqr(v.x,v.y,v.z)));
    for(var v:volumes){if(size==16)break;int i=size++*4;p[i]=(float)(v.x-camera.x);p[i+1]=(float)(v.y-camera.y);p[i+2]=(float)(v.z-camera.z);p[i+3]=v.radius;
     c[i]=v.red;c[i+1]=v.green;c[i+2]=v.blue;c[i+3]=v.density;}
   }
   if(tuning>=0)GL41.glProgramUniform4f(program,tuning,active?VortexConfig.BRIGHTNESS.get()/100f:0,
    active&&VortexConfig.HAZE.get()?VortexConfig.HALO_STRENGTH.get()/100f:0,
    active&&VortexConfig.PIXEL_LIGHTING.get()?1:0,active&&(ready||size>0)?(LavaVisuals.active?2:1):0);
   if(count>=0)GL41.glProgramUniform1i(program,count,size);
   if(positions>=0)GL41.glProgramUniform4fv(program,positions,p);if(colors>=0)GL41.glProgramUniform4fv(program,colors,c);
   return ready?LightField.texture():black();
  },null,"aiVortexField");
  builder.addDynamicSampler(TextureType.TEXTURE_3D,()->VortexBridge.lights()&&LightField.ready()?ShadowGrid.texture():black(),null,"aiVortexOcclusion");
 }
}
