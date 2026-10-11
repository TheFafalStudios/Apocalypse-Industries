import java.nio.file.*;
import java.util.*;
import java.io.*;
import com.euphoriapatches.shadow.org.apache.commons.compress.archivers.tar.*;
import com.euphoriapatches.shadow.io.sigpipe.jbsdiff.ui.FileUI;
public class BuildMatchedEuphoria {
 public static void main(String[] args) throws Exception {
  Path root=Path.of(args[0]), tar=Path.of(args[1]);
  try(TarArchiveOutputStream out=new TarArchiveOutputStream(Files.newOutputStream(tar)); var stream=Files.walk(root)){
   for(Path path:stream.sorted(Comparator.comparing(Path::toUri)).toList()) {
    String name=root.relativize(path).toString().replace(File.separatorChar,'/');
    TarArchiveEntry entry=new TarArchiveEntry(path.toFile(),name);
    entry.setModTime(0);entry.setSize(Files.isRegularFile(path)?Files.size(path):0);
    entry.setIds(0,0);entry.setNames("","");entry.setMode(TarArchiveEntry.DEFAULT_FILE_MODE);
    out.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);out.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);
    out.putArchiveEntry(entry);if(Files.isRegularFile(path))Files.copy(path,out);out.closeArchiveEntry();
   } out.finish();
  }
  System.out.println("Base TAR SHA256="+HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(tar))));
  FileUI.patch(tar.toFile(),new File(args[2]),new File(args[3]));
 }
}
