package dev.apocalypse.submarinelava;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.*;
/** Restores state touched by native field propagation before Iris resumes rendering. */
public final class VortexRenderState implements AutoCloseable {
 private final int draw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING),read=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
 private final int program=GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM),vao=GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
 private final int buffer=GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING),active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
 private final int unpack=GL11.glGetInteger(GL11.GL_UNPACK_ALIGNMENT),pack=GL11.glGetInteger(GL11.GL_PACK_ALIGNMENT);
 private final int[] viewport=new int[4],scissor=new int[4],two=new int[12],three=new int[12],samplers=new int[12];
 private final boolean depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),blend=GL11.glIsEnabled(GL11.GL_BLEND),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),clip=GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
 private final boolean depthMask=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
 private final float[] clearColor=new float[4];
 private final ShaderInstance shader=RenderSystem.getShader();
 public VortexRenderState(){
  GL11.glGetFloatv(GL11.GL_COLOR_CLEAR_VALUE,clearColor);
  GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport); GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX,scissor);
  for(int i=0;i<12;i++){RenderSystem.activeTexture(GL13.GL_TEXTURE0+i);two[i]=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);three[i]=GL11.glGetInteger(GL12.GL_TEXTURE_BINDING_3D);samplers[i]=GL30.glGetIntegeri(GL33.GL_SAMPLER_BINDING,i);GL33.glBindSampler(i,0);}
  RenderSystem.activeTexture(active);
 }
 public void close(){
  GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,draw);GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,read);
  RenderSystem.viewport(viewport[0],viewport[1],viewport[2],viewport[3]);
  GlStateManager._glBindVertexArray(vao);GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER,buffer);
  for(int i=0;i<12;i++){RenderSystem.activeTexture(GL13.GL_TEXTURE0+i);RenderSystem.bindTexture(two[i]);GL11.glBindTexture(GL11.GL_TEXTURE_2D,two[i]);GL11.glBindTexture(GL12.GL_TEXTURE_3D,three[i]);GL33.glBindSampler(i,samplers[i]);}
  RenderSystem.clearColor(clearColor[0],clearColor[1],clearColor[2],clearColor[3]);
  RenderSystem.activeTexture(active);GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT,unpack);GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT,pack);
  if(depth)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();RenderSystem.depthMask(depthMask);
  if(blend)RenderSystem.enableBlend();else RenderSystem.disableBlend();
  if(cull)RenderSystem.enableCull();else RenderSystem.disableCull();
  if(clip)RenderSystem.enableScissor(scissor[0],scissor[1],scissor[2],scissor[3]);else RenderSystem.disableScissor();
  if(shader!=null)RenderSystem.setShader(()->shader);
  GlStateManager._glUseProgram(program);
 }
}
