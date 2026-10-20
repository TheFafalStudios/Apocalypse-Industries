package dev.apocalypse.submarinelava;

import java.nio.file.*;
import java.util.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;

/** Exercise the shipped gap helper on the GPU against the frozen datapack sea level. */
public final class NetherSeaRegression extends VortexRegression {
 public static void main(String[] args) throws Exception {
  check(GLFW.glfwInit(), "GLFW");
  GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
  long window=GLFW.glfwCreateWindow(8,8,"Nether sea regression",0,0);
  try {
   GLFW.glfwMakeContextCurrent(window); GL.createCapabilities();
   String helper=Files.readString(Path.of(args[0]));
   String source="#version 330 compatibility\nuniform float renderDistance; uniform vec3 cameraPosition; uniform int isEyeInWater; uniform vec3 testRay; uniform mat4 gbufferModelViewInverse;\n#define NETHER_VIEW_LIMIT 1250.0\n"
    +helper+"\nvoid main(){gl_FragColor=vec4(GetNetherGapDistance(testRay),0,0,1);}";
   int fs=compile(GL20.GL_FRAGMENT_SHADER,source);
   int vs=compile(GL20.GL_VERTEX_SHADER,"#version 330 compatibility\nvoid main(){vec2 p=gl_VertexID==0?vec2(-1,-1):(gl_VertexID==1?vec2(3,-1):vec2(-1,3));gl_Position=vec4(p,0,1);}");
   int program=GL20.glCreateProgram(); GL20.glAttachShader(program,fs); GL20.glAttachShader(program,vs); GL20.glLinkProgram(program);
   check(GL20.glGetProgrami(program,GL20.GL_LINK_STATUS)!=0,GL20.glGetProgramInfoLog(program));
   GL20.glUseProgram(program); matrix(program,"gbufferModelViewInverse");
   GL20.glUniform1f(uniform(program,"renderDistance"),1000);
   int target=flat(0,0,0,1), fbo=GL30.glGenFramebuffers();
   GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,fbo); GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,GL30.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,target,0);
   GL11.glViewport(0,0,8,8);
   // y, ray x/y/z, medium, expected distance. Below-sea dry cabins must not project Y32.
   float[][] cases={{40,0,-1,0,0,1000},{100,0,-1,0,0,1000},{104,0,-1,0,0,4},
    {200,0,-1,0,0,100},{104,0,1,0,0,1000},{104,1,0,0,0,1000},
    {104,0,-1,0,1,1000},{104,0,-1,0,2,1000},{2000,0,-1,0,0,1000},
    {104,1,-1,0,0,(float)(4*Math.sqrt(2))}};
   for(float[] c:cases){
    GL20.glUniform3f(uniform(program,"cameraPosition"),0,c[0],0);
    GL20.glUniform3f(uniform(program,"testRay"),c[1],c[2],c[3]);
    GL20.glUniform1i(uniform(program,"isEyeInWater"),(int)c[4]);
    float got=pixel(program)[0]; check(Math.abs(got-c[5])<.002,"Gap distance at camera y="+c[0]+": "+got+" expected "+c[5]);
   }
   var definitions=new ArrayList<net.irisshaders.iris.helpers.StringPair>();
   for(String[] p:new String[][]{{"MC_VERSION","12101"},{"MC_GL_VERSION","460"},{"MC_GLSL_VERSION","460"},{"MC_OS_WINDOWS","1"},{"IS_IRIS","1"},{"DISTANT_HORIZONS","1"},{"MC_RENDER_QUALITY","1.0"},{"MC_SHADOW_QUALITY","1.0"}})
    definitions.add(new net.irisshaders.iris.helpers.StringPair(p[0],p[1]));
   String deferred=net.irisshaders.iris.shaderpack.preprocessor.JcppProcessor.glslPreprocessSource(Files.readString(Path.of(args[1])),definitions);
   int compiled=compile(GL20.GL_FRAGMENT_SHADER,deferred); GL20.glDeleteShader(compiled); checks++;
   check(GL11.glGetError()==GL11.GL_NO_ERROR,"GL error");
   System.out.println(checks+" sea-level GPU checks passed; complete Nether deferred fragment compiled; GPU="+GL11.glGetString(GL11.GL_RENDERER));
  } finally {GLFW.glfwDestroyWindow(window); GLFW.glfwTerminate();}
 }
}
