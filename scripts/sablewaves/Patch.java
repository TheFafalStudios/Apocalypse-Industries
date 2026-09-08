import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

public final class Patch implements Opcodes {
    static final String ROOT = "dev/ashok/sablewaves/";
    static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node, 0); return node;
    }
    static MethodNode method(ClassNode c, String name) {
        var found = c.methods.stream().filter(m -> m.name.equals(name)).toList();
        if (found.size() != 1) throw new IllegalStateException(c.name + "." + name + " count=" + found.size());
        return found.get(0);
    }
    static InsnList selfCall(String owner, String name) {
        InsnList list = new InsnList(); list.add(new VarInsnNode(ALOAD, 0));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, owner, name, "()V", false)); return list;
    }
    public static void main(String[] args) throws Exception {
        Path input = Path.of(args[0]), classes = Path.of(args[1]), fragment = Path.of(args[2]), output = Path.of(args[3]);
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (JarFile jar = new JarFile(input.toFile())) {
            for (var entry : Collections.list(jar.entries())) if (!entry.isDirectory())
                entries.put(entry.getName(), jar.getInputStream(entry).readAllBytes());
        }
        int redirected = 0, biomeGuards = 0;
        for (String name : List.of("physics/WaterProbe", "visual/CrestParticles", "visual/OceanEffects")) {
            String entry = ROOT + name + ".class";
            ClassNode node = read(entries.get(entry));
            for (MethodNode m : node.methods) for (AbstractInsnNode insn : m.instructions.toArray()) {
                if (insn instanceof MethodInsnNode call && call.name.equals("getFluidState") &&
                    (call.owner.equals("net/minecraft/server/level/ServerLevel") || call.owner.equals("net/minecraft/world/level/Level"))) {
                    if (!call.desc.equals("(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
                        throw new IllegalStateException("Unexpected fluid descriptor");
                    call.setOpcode(INVOKESTATIC); call.owner = ROOT + "hotfix/ChunkSafety"; call.name = "fluid";
                    call.desc = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;";
                    call.itf = false; redirected++;
                }
            }
            if (name.equals("physics/WaterProbe")) {
                MethodNode m = method(node, "isSeaBiome");
                // getBiome may sample neighboring quart positions. Require the
                // complete 3x3 loaded neighborhood before the original lookup.
                InsnList guard = new InsnList();
                LabelNode safe = new LabelNode();
                guard.add(new VarInsnNode(ALOAD, 0)); guard.add(new VarInsnNode(ALOAD, 1));
                guard.add(new MethodInsnNode(INVOKESTATIC, ROOT + "hotfix/BiomeSafety", "loadedNeighborhood",
                    "(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z", false));
                guard.add(new JumpInsnNode(IFNE, safe)); guard.add(new InsnNode(ICONST_0)); guard.add(new InsnNode(IRETURN));
                guard.add(safe); guard.add(new FrameNode(F_SAME, 0, null, 0, null));
                m.instructions.insert(guard); biomeGuards++;
            }
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); node.accept(writer); entries.put(entry, writer.toByteArray());
        }
        if (redirected != 5 || biomeGuards != 1) throw new IllegalStateException("Unexpected guard counts " + redirected + "/" + biomeGuards);
        ClassNode renderer = read(entries.get(ROOT + "client/WaveOverlayRenderer.class"));
        ClassNode replacements = read(Files.readAllBytes(fragment));
        for (MethodNode m : replacements.methods) {
            if (m.name.equals("acquireSprite") || m.name.startsWith("hotfix")) {
                renderer.methods.removeIf(old -> old.name.equals(m.name) && old.desc.equals(m.desc));
                renderer.methods.add(m);
            }
        }
        MethodNode stage = method(renderer, "onRenderStage");
        InsnList prepare = new InsnList(); prepare.add(new VarInsnNode(ALOAD, 0)); prepare.add(new VarInsnNode(ALOAD, 1));
        prepare.add(new MethodInsnNode(INVOKEVIRTUAL, renderer.name, "hotfixPrepare", "(Lnet/neoforged/neoforge/client/event/RenderLevelStageEvent;)V", false));
        stage.instructions.insert(prepare);
        method(renderer, "releaseTesselator").instructions.insert(selfCall(renderer.name, "hotfixRestoreState"));
        MethodNode vertex = method(renderer, "waterVertex");
        // Derive local slots from the descriptor: x argument 3, z argument 5,
        // and packed water RGB argument 15. No guessed byte offsets.
        Type[] types = Type.getArgumentTypes(vertex.desc); int[] slots = new int[types.length]; int slot = 1;
        for (int i = 0; i < types.length; i++) { slots[i] = slot; slot += types[i].getSize(); }
        if (types[3] != Type.DOUBLE_TYPE || types[5] != Type.DOUBLE_TYPE || types[15] != Type.INT_TYPE)
            throw new IllegalStateException("waterVertex signature changed");
        InsnList color = new InsnList(); color.add(new VarInsnNode(ALOAD, 0));
        color.add(new VarInsnNode(DLOAD, slots[3])); color.add(new VarInsnNode(DLOAD, slots[5]));
        color.add(new VarInsnNode(ILOAD, slots[15]));
        color.add(new MethodInsnNode(INVOKEVIRTUAL, renderer.name, "hotfixColor", "(DDI)I", false));
        color.add(new VarInsnNode(ISTORE, slots[15])); vertex.instructions.insert(color);
        ClassWriter rw = new ClassWriter(ClassWriter.COMPUTE_MAXS); renderer.accept(rw);
        entries.put(ROOT + "client/WaveOverlayRenderer.class", rw.toByteArray());
        try (var files = Files.walk(classes)) {
            for (Path p : files.filter(p -> p.toString().endsWith(".class")).toList()) {
                String name = classes.relativize(p).toString().replace('\\', '/');
                entries.put(name, Files.readAllBytes(p));
            }
        }
        // Data-flow verification without loading/initializing Minecraft classes.
        int methods = 0;
        for (var entry : entries.entrySet()) if (entry.getKey().endsWith(".class")) {
            ClassNode node = read(entry.getValue());
            for (MethodNode m : node.methods) if ((m.access & (ACC_ABSTRACT | ACC_NATIVE)) == 0) {
                try { new Analyzer<>(new BasicVerifier()).analyze(node.name, m); }
                catch (Exception ex) { throw new IllegalStateException(node.name + "." + m.name, ex); }
                methods++;
            }
        }
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(output))) {
            for (var entry : entries.entrySet()) {
                JarEntry e = new JarEntry(entry.getKey()); e.setTime(0); jar.putNextEntry(e); jar.write(entry.getValue()); jar.closeEntry();
            }
        }
        System.out.println("Patched fluid reads=" + redirected + ", biome guards=" + biomeGuards + "; verified methods=" + methods);
    }
}
