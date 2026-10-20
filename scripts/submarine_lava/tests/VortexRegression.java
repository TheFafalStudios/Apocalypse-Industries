package dev.apocalypse.submarinelava;
import java.nio.file.*;
import java.util.*;
import org.lwjgl.opengl.*;
import org.lwjgl.glfw.GLFW;
public class VortexRegression {
 static final float[] ID={1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};
 static int checks;
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);checks++;}
 static int compile(int type,String source){int s=GL20.glCreateShader(type);GL20.glShaderSource(s,source);GL20.glCompileShader(s);
  if(GL20.glGetShaderi(s,GL20.GL_COMPILE_STATUS)==0)throw new AssertionError(GL20.glGetShaderInfoLog(s));return s;}
 static int uniform(int p,String name){return GL20.glGetUniformLocation(p,name);}
 static void matrix(int p,String name){int u=uniform(p,name);if(u>=0)GL20.glUniformMatrix4fv(u,false,ID);}
 static int volume(float r,float g,float b,float a){int t=GL45.glCreateTextures(GL12.GL_TEXTURE_3D);GL45.glTextureStorage3D(t,1,GL30.GL_RGBA16F,2,2,2);
  GL44.glClearTexImage(t,0,GL11.GL_RGBA,GL11.GL_FLOAT,new float[]{r,g,b,a});GL45.glTextureParameteri(t,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);GL45.glTextureParameteri(t,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);return t;}
 static int flat(float r,float g,float b,float a){int t=GL45.glCreateTextures(GL11.GL_TEXTURE_2D);GL45.glTextureStorage2D(t,1,GL30.GL_RGBA32F,8,8);
  GL44.glClearTexImage(t,0,GL11.GL_RGBA,GL11.GL_FLOAT,new float[]{r,g,b,a});GL45.glTextureParameteri(t,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL45.glTextureParameteri(t,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);return t;}
 static void sampler(int p,String name,int unit,int texture){int u=uniform(p,name);if(u>=0){GL20.glUniform1i(u,unit);GL45.glBindTextureUnit(unit,texture);}}
 static float[] pixel(int p){GL20.glUseProgram(p);GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);float[] v=new float[4];GL11.glReadPixels(4,4,1,1,GL11.GL_RGBA,GL11.GL_FLOAT,v);return v;}
 public static void main(String[] args)throws Exception{
  check(GLFW.glfwInit(),"GLFW");GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE,GLFW.GLFW_FALSE);long w=GLFW.glfwCreateWindow(8,8,"Vortex bridge tests",0,0);
  GLFW.glfwMakeContextCurrent(w);GL.createCapabilities();com.mojang.blaze3d.systems.RenderSystem.initRenderThread();
  try{
   String helper=Files.readString(Path.of(args[0]));
   // Full native shader files: repair the reserved identifier, including a negative control.
   Path nativeDir=Path.of(args[1]);
   for(Path f:Files.list(nativeDir).filter(f->f.toString().endsWith(".fsh")).toList()){
    String s=Files.readString(f);if(f.getFileName().toString().equals("vortex_pixel.fsh")){
     int bad=GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);GL20.glShaderSource(bad,s);GL20.glCompileShader(bad);
     check(GL20.glGetShaderi(bad,GL20.GL_COMPILE_STATUS)==0,"Original Vortex pixel shader reproduces failure");GL20.glDeleteShader(bad);s=s.replaceAll("\\bpacked\\b","aiPackedTile");
    }
    int shader=compile(GL20.GL_FRAGMENT_SHADER,s);GL20.glDeleteShader(shader);checks++;
   }
   String header="#version 330 compatibility\n#define FRAGMENT_SHADER\n";
   String uniforms="uniform mat4 gbufferProjectionInverse,gbufferModelViewInverse,dhProjectionInverse; uniform vec3 cameraPosition; uniform float frameTimeCounter; uniform int isEyeInWater; uniform sampler2D colortex0,colortex6,colortex15,dhDepthTex1;\n";
   for(String defines:new String[]{"#define OVERWORLD\n","#define NETHER\n","#define END\n","#define OVERWORLD\n#define DISTANT_HORIZONS\n","#define NETHER\n#define DISTANT_HORIZONS\n"}){
    String s=header+defines+uniforms+helper+"\nvoid main(){vec2 uv=gl_FragCoord.xy/8.0; vec3 color=texture2D(colortex0,uv).rgb; AiApplyVortex(color,uv,0.75,0.75);gl_FragColor=vec4(color,1.0);}";
    s=net.irisshaders.iris.shaderpack.preprocessor.JcppProcessor.glslPreprocessSource(s,List.of());
    int fragment=compile(GL20.GL_FRAGMENT_SHADER,s);
    int vertex=compile(GL20.GL_VERTEX_SHADER,"#version 330 compatibility\nvoid main(){vec2 p=gl_VertexID==0?vec2(-1,-1):(gl_VertexID==1?vec2(3,-1):vec2(-1,3));gl_Position=vec4(p,0,1);}");
    int program=GL20.glCreateProgram();GL20.glAttachShader(program,fragment);GL20.glAttachShader(program,vertex);GL20.glLinkProgram(program);
    check(GL20.glGetProgrami(program,GL20.GL_LINK_STATUS)!=0,GL20.glGetProgramInfoLog(program));
    GL20.glUseProgram(program);
    matrix(program,"gbufferProjectionInverse");matrix(program,"gbufferModelViewInverse");matrix(program,"dhProjectionInverse");
    int base=flat(.2f,.2f,.2f,1),empty=flat(0,0,0,1),far=flat(1,1,1,1),field=volume(.2f,0,0,.2f),open=volume(0,0,0,0),solid=volume(1,1,1,1);
    sampler(program,"colortex0",0,base);sampler(program,"aiVortexField",1,field);sampler(program,"aiVortexOcclusion",2,open);sampler(program,"aiVortexHull",3,far);
    sampler(program,"colortex6",4,empty);sampler(program,"dhDepthTex1",5,far);sampler(program,"colortex15",6,far);
    int target=flat(0,0,0,1),fbo=GL30.glGenFramebuffers();GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,fbo);GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,GL30.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,target,0);
    check(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)==GL30.GL_FRAMEBUFFER_COMPLETE,"Test FBO");GL11.glViewport(0,0,8,8);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_BLEND);
    GL20.glUniform4f(uniform(program,"aiVortexTuning"),1,1,0,0);GL20.glUniform4f(uniform(program,"aiVortexGrid"),64,64,64,1);
    float[] a=pixel(program);check(Math.abs(a[0]-.2)<.001&&Math.abs(a[1]-.2)<.001,"Inactive bridge preserves scene");
    GL20.glUniform4f(uniform(program,"aiVortexTuning"),1,1,0,1);float[] lit=pixel(program);check(lit[0]>a[0]+.03&&Math.abs(lit[1]-a[1])<.001,"Red field adds red surface light");
    float[] shifted=ID.clone();shifted[12]=300;GL20.glUniformMatrix4fv(uniform(program,"gbufferModelViewInverse"),false,shifted);
    float[] shiftedPixel=pixel(program);check(Math.abs(shiftedPixel[0]-.2)<.001,"Inverse-view translation respected for field alignment");
    matrix(program,"gbufferModelViewInverse");
    sampler(program,"aiVortexOcclusion",2,solid);float[] blocked=pixel(program);check(Math.abs(blocked[0]-.2)<.001,"Solid cell suppresses colored light");sampler(program,"aiVortexOcclusion",2,open);
    GL20.glUniform4f(uniform(program,"aiVortexGrid"),64,64,64,0);float[] missing=pixel(program);check(Math.abs(missing[0]-.2)<.001,"Unready grid preserves scene");
    GL20.glUniform4f(uniform(program,"aiVortexGrid"),-200,-200,-200,1);float[] outside=pixel(program);check(Math.abs(outside[0]-.2)<.001,"Outside field bounds preserves scene");
    GL20.glUniform4f(uniform(program,"aiVortexGrid"),64,64,64,1);GL20.glUniform1i(uniform(program,"isEyeInWater"),1);float[] water=pixel(program);check(water[0]>lit[0],"Underwater field adds haze");
    GL20.glUniform1i(uniform(program,"isEyeInWater"),2);float[] lava=pixel(program);check(lava[0]<water[0],"Lava attenuates light and haze");
    GL20.glUniform1i(uniform(program,"isEyeInWater"),0);GL20.glUniform4f(uniform(program,"aiVortexGrid"),64,64,64,0);
    GL20.glUniform1i(uniform(program,"aiMistCount"),1);
    GL20.glUniform4f(uniform(program,"aiMistPosition[0]"),0,0,.25f,2);
    GL20.glUniform4f(uniform(program,"aiMistColor[0]"),0,1,0,10);
    float[] mist=pixel(program);check(mist[0]<.2 && mist[1]>mist[0],"Native mist volume contributes colored fog");
    int cabin=flat(.9f,.9f,.9f,1);sampler(program,"aiVortexHull",3,cabin);
    float[] interior=pixel(program);check(Math.abs(interior[0]-.2)<.001&&Math.abs(interior[1]-.2)<.001,"Hull keeps cabin mist-free");
    sampler(program,"aiVortexHull",3,far);GL20.glUniform1i(uniform(program,"aiMistCount"),0);GL11.glDeleteTextures(cabin);
    int before=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);int[] viewport=new int[4];GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);
    int nativeSampler=GL33.glGenSamplers();GL33.glBindSampler(5,nativeSampler);
    try(VortexRenderState saved=new VortexRenderState()){
     check(GL30.glGetIntegeri(GL33.GL_SAMPLER_BINDING,5)==0,"Native propagation isolated from Iris sampler objects");
     GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,0);com.mojang.blaze3d.systems.RenderSystem.viewport(2,3,4,5);com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
    }
    check(GL30.glGetIntegeri(GL33.GL_SAMPLER_BINDING,5)==nativeSampler,"Iris sampler object restored");GL33.glBindSampler(5,0);GL33.glDeleteSamplers(nativeSampler);
    check(GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING)==before,"Framebuffer restored");int[] after=new int[4];GL11.glGetIntegerv(GL11.GL_VIEWPORT,after);check(Arrays.equals(after,viewport),"Viewport restored");
    var builder=net.irisshaders.iris.gl.program.ProgramSamplers.builder(program,Set.of(8,9));builder.addDefaultSampler(net.irisshaders.iris.gl.texture.TextureType.TEXTURE_2D,()->base,null,null,"colortex0");
    VortexSamplers.register(builder,program);builder.build();checks++;
    GL20.glUseProgram(0);GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,0);GL30.glDeleteFramebuffers(fbo);GL20.glDeleteProgram(program);GL20.glDeleteShader(fragment);GL20.glDeleteShader(vertex);
    for(int t:new int[]{base,empty,far,field,open,solid,target})GL11.glDeleteTextures(t);
   }
   fogGate();
   for(int file=2;file<args.length;file++){
    String s=Files.readString(Path.of(args[file]));
    var definitions=new java.util.ArrayList<net.irisshaders.iris.helpers.StringPair>();
    for(String[] pair:new String[][]{{"MC_VERSION","12101"},{"MC_GL_VERSION","460"},{"MC_GLSL_VERSION","460"},{"MC_OS_WINDOWS","1"},{"IS_IRIS","1"},{"DISTANT_HORIZONS","1"},{"MC_RENDER_QUALITY","1.0"},{"MC_SHADOW_QUALITY","1.0"}})
     definitions.add(new net.irisshaders.iris.helpers.StringPair(pair[0],pair[1]));
    s=net.irisshaders.iris.shaderpack.preprocessor.JcppProcessor.glslPreprocessSource(s,definitions);
    Files.writeString(Path.of(args[file]+".preprocessed"),s);
    // Apply the installed upstream water patch and our lava patch at the real late stage.
    var support=Class.forName("com.maxenonyme.createsubmarine.submarine.compat.iris.ComplementarySupport");
    var method=support.getDeclaredMethod("patch",String.class);method.setAccessible(true);
    String water=(String)method.invoke(null,s);
    check(!water.equals(s),"Full shader remains recognized by Deep Seas");
    String constants=new String(VortexRegression.class.getClassLoader().getResourceAsStream(
        "com/maxenonyme/createsubmarine/submarine/compat/iris/ShaderpackSupport.class").readAllBytes(),java.nio.charset.StandardCharsets.ISO_8859_1);
    int begin=constants.indexOf("float cdsWaterBehindHull(vec2 cdsUV, float sceneDepth) {\n");
    int finish=constants.indexOf("\n}\n\n",begin);
    var main=java.util.regex.Pattern.compile("\\bvoid\\s+main\\s*\\(").matcher(water);check(main.find(),"Full shader main");
    s=water.substring(0,main.start())+"uniform sampler2D CdsHullFar;\n"+constants.substring(begin,finish+4)+water.substring(main.start());
    s=ShaderPatch.patch(s);
    int shader=compile(GL20.GL_FRAGMENT_SHADER,s);
    int linked=GL20.glCreateProgram();GL20.glAttachShader(linked,shader);GL20.glLinkProgram(linked);
    check(GL20.glGetProgrami(linked,GL20.GL_LINK_STATUS)!=0,GL20.glGetProgramInfoLog(linked));
    GL20.glDeleteProgram(linked);GL20.glDeleteShader(shader);checks++;
   }
   check(GL11.glGetError()==GL11.GL_NO_ERROR,"No GL error after integration tests");
   System.out.println(checks+" Vortex GPU/native-shader/state/registration checks passed");
  }finally{GLFW.glfwDestroyWindow(w);GLFW.glfwTerminate();}
 }
 static void fogGate() throws Exception {
  String constants=new String(VortexRegression.class.getClassLoader().getResourceAsStream(
   "com/maxenonyme/createsubmarine/submarine/compat/iris/ShaderpackSupport.class").readAllBytes(),java.nio.charset.StandardCharsets.ISO_8859_1);
  int begin=constants.indexOf("float cdsWaterBehindHull(vec2 cdsUV, float sceneDepth) {\n");
  int finish=constants.indexOf("\n}\n\n",begin);
  String source="#version 330 compatibility\nuniform sampler2D CdsHullFar;\nuniform mat4 gbufferProjectionInverse; uniform int isEyeInWater; uniform float z1; uniform vec2 texCoord;\n"
   +constants.substring(begin,finish+4)+"\nvoid main(){vec4 color=vec4(1); vec4 volumetricEffect=vec4(1); float cdsWater = cdsWaterBehindHull(texCoord, z1); gl_FragColor=vec4(float(cdsWater>0),float(aiLavaBehindHull(texCoord,z1)>0),0,1);}";
  source=ShaderPatch.patch(source);
  int fs=compile(GL20.GL_FRAGMENT_SHADER,source),vs=compile(GL20.GL_VERTEX_SHADER,"#version 330 compatibility\nvoid main(){vec2 p=gl_VertexID==0?vec2(-1,-1):(gl_VertexID==1?vec2(3,-1):vec2(-1,3));gl_Position=vec4(p,0,1);}");
  int program=GL20.glCreateProgram();GL20.glAttachShader(program,fs);GL20.glAttachShader(program,vs);GL20.glLinkProgram(program);
  check(GL20.glGetProgrami(program,GL20.GL_LINK_STATUS)!=0,GL20.glGetProgramInfoLog(program));GL20.glUseProgram(program);
  int invalid=flat(0,0,0,1),outside=flat(1,1,1,1),target=flat(0,0,0,1),fbo=GL30.glGenFramebuffers();
  GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,fbo);GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,GL30.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,target,0);GL11.glViewport(0,0,8,8);
  sampler(program,"aiWaterHullFar",0,invalid);sampler(program,"CdsLavaHullFar",1,invalid);matrix(program,"gbufferProjectionInverse");GL20.glUniform1f(uniform(program,"z1"),1);GL20.glUniform2f(uniform(program,"texCoord"),.5f,.5f);
  for(int medium=0;medium<3;medium++){
   GL20.glUniform1i(uniform(program,"aiHullMedium"),medium);float[] v=pixel(program);
   check(Math.abs(v[0]-(medium==1?1:0))<.001 && Math.abs(v[1]-(medium==2?1:0))<.001,"Bad hull texture cannot fog wrong cabin medium: "+medium);
  }
  sampler(program,"aiWaterHullFar",0,outside);sampler(program,"CdsLavaHullFar",1,outside);
  for(int medium=1;medium<3;medium++){
   GL20.glUniform1i(uniform(program,"aiHullMedium"),medium);float[] v=pixel(program);check(Math.abs(v[0])+Math.abs(v[1])<.001,"Outside mask preserves both media");
  }
  GL20.glUseProgram(0);GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,0);GL30.glDeleteFramebuffers(fbo);GL20.glDeleteProgram(program);GL20.glDeleteShader(fs);GL20.glDeleteShader(vs);for(int t:new int[]{invalid,outside,target})GL11.glDeleteTextures(t);
 }

}
