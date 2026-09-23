import re,json
def parse(text):
 tokens=re.findall(r'"(?:\\.|[^"\\])*"|[{}\[\]:,]|[^\s{}\[\]:,]+',text);i=0
 def value():
  nonlocal i
  t=tokens[i];i+=1
  if t=='{':
   d={}
   while tokens[i]!='}':
    if tokens[i]==',':i+=1;continue
    k=tokens[i];i+=1;k=json.loads(k) if k.startswith('"') else k
    assert tokens[i]==':';i+=1;d[k]=value()
   i+=1;return d
  if t=='[':
   a=[]
   while tokens[i]!=']':
    if tokens[i]==',':i+=1;continue
    a.append(value())
   i+=1;return a
  if t.startswith('"'):return json.loads(t)
  if t in ('true','false'):return t=='true'
  try:return float(re.sub(r'[bBdDfFlLsS]$','',t))
  except ValueError:return t
 result=value();assert i==len(tokens);return result


def canonical_counts(value):
 """FTB omits default counts of one when saving item tasks and rewards."""
 if isinstance(value,dict):
  return {k:canonical_counts(v) for k,v in value.items() if not (k=='count' and v==1)}
 if isinstance(value,list):return [canonical_counts(v) for v in value]
 return value
