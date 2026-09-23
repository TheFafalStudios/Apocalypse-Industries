# Export generated face; retain approved native alpha, outer edge and bolt regions.
Add-Type -AssemblyName System.Drawing
$root=Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$old=[Drawing.Bitmap]::new((Join-Path $root 'docs/assets/echo_plate_v1.png'))
$src=[Drawing.Bitmap]::new((Join-Path $root 'docs/assets/echo_plate_generated_v2.png'))
$target=[Drawing.Bitmap]::new(16,16,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
$palette=@('0D1117','111A21','041A1D','052832','033D50','0A4B60','0D626C','008A95','29D4EB') | ForEach-Object {[Drawing.ColorTranslator]::FromHtml('#'+$_)}
$bolts=@(@(5,3),@(12,3),@(3,10),@(9,10))
for($y=0;$y -lt 16;$y++){for($x=0;$x -lt 16;$x++){
 $original=$old.GetPixel($x,$y);if($original.A -eq 0){continue}
 $preserve=$false
 foreach($d in @(@(-1,0),@(1,0),@(0,-1),@(0,1))){if($old.GetPixel($x+$d[0],$y+$d[1]).A -eq 0){$preserve=$true}}
 foreach($bolt in $bolts){if([Math]::Abs($x-$bolt[0]) -le 1 -and [Math]::Abs($y-$bolt[1]) -le 1){$preserve=$true}}
 if($preserve){$target.SetPixel($x,$y,$original);continue}
 $pixel=$src.GetPixel([int][Math]::Floor(73+($x-0.5)*1135/14),[int][Math]::Floor(80+($y-0.5)*1078/13))
 # Retain small generated sparkle highlights that center-only sampling can miss.
 for($sy=0;$sy -lt 5;$sy++){for($sx=0;$sx -lt 5;$sx++){
  $probe=$src.GetPixel([int][Math]::Floor(73+($x-1+($sx+0.5)/5)*1135/14),[int][Math]::Floor(80+($y-1+($sy+0.5)/5)*1078/13))
  if($probe.R -gt 20 -and $probe.G -gt 180 -and $probe.B -gt 200 -and $probe.G -gt $pixel.G){$pixel=$probe}
 }}
 $best=$palette[0];$distance=[double]::PositiveInfinity
 foreach($c in $palette){$d=[Math]::Pow($pixel.R-$c.R,2)+[Math]::Pow($pixel.G-$c.G,2)+[Math]::Pow($pixel.B-$c.B,2);if($d -lt $distance){$distance=$d;$best=$c}}
 $target.SetPixel($x,$y,$best)
}}
$target.Save((Join-Path $root 'scripts/protection_pixel/resources/assets/apocalypse_pp/textures/item/echo_plate.png'),[Drawing.Imaging.ImageFormat]::Png)
$preview=[Drawing.Bitmap]::new(256,256,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
for($y=0;$y -lt 256;$y++){for($x=0;$x -lt 256;$x++){$preview.SetPixel($x,$y,$target.GetPixel([int][Math]::Floor($x/16),[int][Math]::Floor($y/16)))}}
$preview.Save((Join-Path $root 'docs/assets/echo_plate_preview_v2.png'),[Drawing.Imaging.ImageFormat]::Png)
$old.Dispose();$src.Dispose();$target.Dispose();$preview.Dispose()

