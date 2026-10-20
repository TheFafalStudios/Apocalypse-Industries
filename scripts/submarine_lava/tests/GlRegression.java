package dev.apocalypse.submarinelava;
import java.nio.file.Files;
import java.nio.file.Path;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20;
public final class GlRegression {
    public static void main(String[] args) throws Exception {
        String transformed = Files.readString(Path.of(args[0]));
        int start = transformed.indexOf("float aiLavaBehindHull(");
        String helper = transformed.substring(start, transformed.indexOf("\n}", start) + 2);
        helper = helper.replace("    if (aiHullMedium != 2) return -1.0;\n", "");
        helper = helper.replace("aiLavaBehindHull", "cdsWaterBehindHull").replace("CdsLavaHullFar", "CdsHullFar");
        if (!GLFW.glfwInit()) throw new AssertionError("GLFW init");
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        long window = GLFW.glfwCreateWindow(1, 1, "Shader validation", 0, 0);
        if (window == 0) throw new AssertionError("GL context");
        try {
            GLFW.glfwMakeContextCurrent(window);
            GL.createCapabilities();
            com.mojang.blaze3d.systems.RenderSystem.initRenderThread();
            for (int options = 0; options < 4; options++) {
                boolean reduced = (options & 1) != 0, colored = (options & 2) != 0;
                String source = "#version 120\n" + """
                    uniform sampler2D CdsHullFar;
                    uniform mat4 gbufferProjectionInverse;
                    uniform int isEyeInWater;
                    uniform float z1;
                    varying vec2 texCoord;
                    """ + helper + (reduced ? "\nfloat reducedFog(float fog) { fog = sqrt(fog) * 0.4; return fog; }\n" : "")
                    + "\nvoid main() {\nvec4 color = vec4(1.0);\nvec4 volumetricEffect = vec4(1.0);\n"
                    + (colored ? "vec3 lightFog = vec3(1.0);\n" : "")
                    + "float cdsWater = cdsWaterBehindHull(texCoord, z1);\n"
                    + "gl_FragColor = color + volumetricEffect * 0.01;\n}";
                source = ShaderPatch.patch(source);
                // Use Iris' actual AST transformer directly: the public wrapper also
                // initializes the running NeoForge platform just to read debug settings.
                var field = net.irisshaders.iris.pipeline.transform.TransformPatcher.class.getDeclaredField("transformer");
                field.setAccessible(true);
                @SuppressWarnings("unchecked")
                var transformer = (io.github.douira.glsl_transformer.ast.transform.EnumASTTransformer<
                    net.irisshaders.iris.pipeline.transform.parameter.Parameters,
                    net.irisshaders.iris.pipeline.transform.PatchShaderType>)field.get(null);
                var parsed = transformer.parseTranslationUnit(io.github.douira.glsl_transformer.ast.query.RootSupplier.DEFAULT, source);
                if (parsed == null) throw new AssertionError("Iris parser returned no shader");
                boolean rejected = false;
                try {
                    transformer.parseTranslationUnit(io.github.douira.glsl_transformer.ast.query.RootSupplier.DEFAULT,
                        source + "\n#ifdef LATE_DIRECTIVE\n#endif\n");
                } catch (IllegalArgumentException expected) { rejected = true; }
                if (!rejected) throw new AssertionError("Iris filter must reject the original failure");
                int shader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
                GL20.glShaderSource(shader, source);
                GL20.glCompileShader(shader);
                if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0)
                    throw new AssertionError(GL20.glGetShaderInfoLog(shader));
                GL20.glDeleteShader(shader);
            }
            System.out.println("4 preprocessed lava fog option combinations passed installed Iris parser/token filter and compiled on " + org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER));
            checkSamplerScope(false);
            checkSamplerScope(true);
            System.out.println("GPU-linked Iris sampler registration: ordinary program unchanged; water/lava program uses two managed samplers");
        } finally {
            GLFW.glfwDestroyWindow(window);
            GLFW.glfwTerminate();
        }
    }
    private static void checkSamplerScope(boolean lava) throws Exception {
        int shader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(shader, "#version 120\nuniform sampler2D colortex0;\n" + (lava
            ? "uniform sampler2D CdsLavaHullFar,aiWaterHullFar; void main() { gl_FragColor = texture2D(colortex0, vec2(0.5)) + texture2D(CdsLavaHullFar, vec2(0.5)) + texture2D(aiWaterHullFar, vec2(0.5)); }"
            : "void main() { gl_FragColor = texture2D(colortex0, vec2(0.5)); }"));
        GL20.glCompileShader(shader);
        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, shader);
        GL20.glLinkProgram(program);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == 0) throw new AssertionError(GL20.glGetProgramInfoLog(program));
        try {
            var reserved = java.util.Set.of(8, 9);
            var builder = net.irisshaders.iris.gl.program.ProgramSamplers.builder(program, reserved);
            // Actual Iris lifecycle: default color texture gets unit 0 first.
            builder.addDefaultSampler(net.irisshaders.iris.gl.texture.TextureType.TEXTURE_2D,
                () -> 0, null, null, "colortex0");
            var remaining = builder.getClass().getDeclaredField("remainingUnits");
            remaining.setAccessible(true);
            int before = remaining.getInt(builder);
            LavaSamplers.register(builder, program);
            if (remaining.getInt(builder) != before - (lava ? 2 : 0)) throw new AssertionError("Wrong sampler scope");
            if (!reserved.equals(java.util.Set.of(8, 9))) throw new AssertionError("Reserved units changed");
            builder.build();
            if (lava) {
                var oldOrder = net.irisshaders.iris.gl.program.ProgramSamplers.builder(program, reserved);
                LavaSamplers.register(oldOrder, program);
                boolean rejected = false;
                try {
                    oldOrder.addDefaultSampler(net.irisshaders.iris.gl.texture.TextureType.TEXTURE_2D,
                        () -> 0, null, null, "colortex0");
                } catch (IllegalStateException expected) {
                    rejected = expected.getMessage().contains("Texture unit 0 is already used");
                }
                if (!rejected) throw new AssertionError("Must reproduce 1.0.3 registration-order failure");
            }
        } finally { GL20.glDeleteProgram(program); GL20.glDeleteShader(shader); }
    }
}

