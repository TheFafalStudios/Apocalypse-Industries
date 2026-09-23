# Export the approved image-edit result onto the native 16x16 item grid.
Add-Type -AssemblyName System.Drawing
$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$src = [Drawing.Bitmap]::new((Join-Path $root 'docs/assets/echo_plate_scaled_source.png'))
$minX=$src.Width; $minY=$src.Height; $maxX=0; $maxY=0
for($y=0;$y -lt $src.Height;$y++){for($x=0;$x -lt $src.Width;$x++){if($src.GetPixel($x,$y).A -ge 128){$minX=[Math]::Min($minX,$x);$minY=[Math]::Min($minY,$y);$maxX=[Math]::Max($maxX,$x);$maxY=[Math]::Max($maxY,$y)}}}
$dst=[Drawing.Bitmap]::new(16,16,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
for($y=0;$y -lt 14;$y++){for($x=0;$x -lt 14;$x++){
 $sx=[int][Math]::Floor($minX+($x+0.5)*($maxX-$minX+1)/14)
 $sy=[int][Math]::Floor($minY+($y+0.5)*($maxY-$minY+1)/14)
 $c=$src.GetPixel($sx,$sy)
 # Keep small cyan bolts/sparkles that straddle the reduced grid instead of dropping them.
 $brightCount=0; $bright=$c
 for($v=0;$v -lt 3;$v++){for($u=0;$u -lt 3;$u++){
  $tx=[int][Math]::Floor($minX+($x+($u+0.5)/3)*($maxX-$minX+1)/14)
  $ty=[int][Math]::Floor($minY+($y+($v+0.5)/3)*($maxY-$minY+1)/14)
  $sample=$src.GetPixel($tx,$ty)
  if($sample.A -ge 128 -and $sample.G -gt 160 -and $sample.B -gt 160){$brightCount++;$bright=$sample}
 }}
 if($brightCount -ge 3){$c=$bright}
 if($c.A -ge 128){$dst.SetPixel($x+1,$y+1,[Drawing.Color]::FromArgb(255,$c.R,$c.G,$c.B))}
}}
$dst.Save((Join-Path $root 'scripts/protection_pixel/resources/assets/apocalypse_pp/textures/item/echo_plate.png'),[Drawing.Imaging.ImageFormat]::Png)
$preview=[Drawing.Bitmap]::new(256,256,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
for($y=0;$y -lt 256;$y++){for($x=0;$x -lt 256;$x++){$preview.SetPixel($x,$y,$dst.GetPixel([int][Math]::Floor($x/16),[int][Math]::Floor($y/16)))}}
$preview.Save((Join-Path $root 'docs/assets/echo_plate_scaled_preview.png'),[Drawing.Imaging.ImageFormat]::Png)
$src.Dispose();$dst.Dispose();$preview.Dispose()
