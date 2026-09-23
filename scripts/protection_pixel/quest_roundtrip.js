ServerEvents.commandRegistry(event => {
 event.register(event.commands.literal("ppquesttest").executes(ctx => {
 var server=ctx.source.server;
  try {
   var ppNativeQuestFile = Java.loadClass('dev.ftb.mods.ftbquests.quest.ServerQuestFile');
   var checks = 0;
   var check = (ok, label) => { if (!ok) throw new Error(label); checks++; };
   var verify = () => {
    var file = ppNativeQuestFile.INSTANCE;
    var all = {};
    file.getAllObjects().forEach(o => { all[String(o.getCodeString())] = o; });
    ppObjectIds.forEach(id => check(all[id] != null, 'Stable object ID ' + id));
    ppQuestExpected.forEach(row => {
     var quest = all[row.id];
     row.task_titles.forEach((title,i) => check(String(all[row.runtime_tasks[i].id].getRawTitle()) === title, 'Task label '+row.key+' '+i));
     check(String(quest.getRawTitle()) === row.title, 'Title ' + row.key);
     check(String(quest.getRawSubtitle()) === row.subtitle, 'Subtitle '+row.key);
     var desc = quest.getRawDescription();
     check(desc.size() === row.text.length, 'Description length ' + row.key);
     for (var i=0; i<row.text.length; i++) check(String(desc.get(i)) === row.text[i], 'Description wording '+row.key+' '+i);
     var deps = quest.streamDependencies().toList();
     check(deps.size() === row.parent_ids.length, 'Prerequisite count '+row.key);
     row.parent_ids.forEach(id => check(quest.hasDependency(all[id]), 'Prerequisite '+row.key+' '+id));
    });
   };
   verify();
   console.info('PP QUEST RUNTIME BEFORE SAVE PASS: '+checks+' checks');
   var file = ppNativeQuestFile.INSTANCE;
   file.writeDataFull(file.getFolder(), server.registryAccess());
   file.load();
   verify();
   console.info('PP QUEST ROUNDTRIP PASS: '+checks+' checks; exact titles, subtitles, task labels and stable IDs');
  } catch (error) { console.error('PP QUEST ROUNDTRIP FAIL: '+error); }

 return 1;
 }));
});
