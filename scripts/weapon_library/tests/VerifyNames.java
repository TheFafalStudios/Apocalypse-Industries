import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import apocalypse.tridents.LibraryTridentNames;
public class VerifyNames {
 record Rule(String item,String model,String name,Pattern pattern) {}
 static List<Rule> rules = new ArrayList<>();
 static int checks=0;
 static String decode(String s){return new String(Base64.getDecoder().decode(s),StandardCharsets.UTF_8);}
 static Set<String> models(String item,String name){var out=new HashSet<String>();for(var r:rules)if(r.item.equals(item)&&r.pattern.matcher(name).matches())out.add(r.model);return out;}
 static void check(boolean ok,String info){checks++;if(!ok)throw new AssertionError(info);}
 public static void main(String[] args)throws Exception{
  for(String line:Files.readAllLines(Path.of(args[0]))){var a=line.split("\t");rules.add(new Rule(decode(a[0]),decode(a[1]),decode(a[2]),Pattern.compile(decode(a[3]))));}
  for(var r:rules){
   var baseline=models(r.item,r.name);
   check(baseline.equals(Set.of(r.model)),"Ambiguous or missing original alias: "+r.name+" on "+r.item+": "+baseline);
   var variants=new ArrayList<>(List.of("My "+r.name,"My "+r.name+" of Power +1",r.name+" +1","\u00a7b"+r.name+"\u00a7r", "My \u00a7l"+r.name.replace(" ","\u00a7r \u00a7b")+"\u00a7r +1",r.name.chars().mapToObj(c->"\u00a7b"+(char)c).reduce("",String::concat)+"\u00a7r"));
   if((r.pattern.flags() & Pattern.CASE_INSENSITIVE)!=0)variants.add(r.name.toLowerCase(Locale.ROOT));
   for(var name:variants)check(models(r.item,name).equals(baseline),"Wrong prefixed/formatted model: "+name+" on "+r.item+": "+models(r.item,name));
   check(!r.pattern.matcher("X"+r.name+"X").matches(),"Matched embedded word: "+r.name);
  }
  check(!LibraryTridentNames.contains(null),"Null trident");
  for(String name:List.of("My Crystal Trident +1","\u00a7bAquatic Trident\u00a7r of Power","ocean rage +1","my PHANTOMGUARD TRIDENT"))check(LibraryTridentNames.contains(name),"Missing fuzzy trident: "+name);
  for(String name:List.of("Trident","Heavenly Trident","Crimson Lance","XCrystal TridentX"))check(!LibraryTridentNames.contains(name),"Unexpected trident: "+name);
  System.out.println("PASS: "+checks+" Java Pattern matching checks across "+rules.size()+" item/name rules.");
 }
}