import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public final class LinkageAudit {
    static Map<String,ClassNode> cache = new HashMap<>();
    static ClassNode resolve(String name) throws Exception {
        if (cache.containsKey(name)) return cache.get(name);
        try (var input = ClassLoader.getSystemResourceAsStream(name + ".class")) {
            if (input == null) { cache.put(name, null); return null; }
            ClassNode c = new ClassNode(); new ClassReader(input).accept(c, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            cache.put(name, c); return c;
        }
    }
    static boolean member(String owner, String name, String desc, boolean method, Set<String> seen) throws Exception {
        if (owner.startsWith("[")) return true;
        if (!seen.add(owner)) return false;
        ClassNode c = resolve(owner); if (c == null) return false;
        if (method ? c.methods.stream().anyMatch(m -> m.name.equals(name) && m.desc.equals(desc))
                   : c.fields.stream().anyMatch(f -> f.name.equals(name) && f.desc.equals(desc))) return true;
        if (name.equals("<init>")) return false;
        if (c.superName != null && member(c.superName, name, desc, method, seen)) return true;
        for (String parent : c.interfaces) if (member(parent, name, desc, method, seen)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        int checked = 0; Set<String> failures = new TreeSet<>();
        try (JarFile jar = new JarFile(args[0])) {
            for (var e : Collections.list(jar.entries())) if (e.getName().endsWith(".class")) {
                ClassNode c = new ClassNode(); new ClassReader(jar.getInputStream(e)).accept(c, 0);
                for (MethodNode m : c.methods) for (AbstractInsnNode ins : m.instructions) {
                    String owner, name, desc; boolean isMethod;
                    if (ins instanceof MethodInsnNode call) { owner=call.owner; name=call.name; desc=call.desc; isMethod=true; }
                    else if (ins instanceof FieldInsnNode field) { owner=field.owner; name=field.name; desc=field.desc; isMethod=false; }
                    else continue;
                    if (owner.startsWith("java/") || owner.startsWith("jdk/") || owner.startsWith("[")) continue;
                    checked++;
                    if (!member(owner, name, desc, isMethod, new HashSet<>()))
                        failures.add(c.name + "." + m.name + " -> " + owner + "." + name + desc);
                }
            }
        }
        System.out.println("Checked external/member references: " + checked + "; unresolved: " + failures.size());
        failures.forEach(System.out::println);
        if (!failures.isEmpty()) System.exit(1);
    }
}
