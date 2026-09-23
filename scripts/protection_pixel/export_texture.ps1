# Mechanical pixel export of the generated sprite; no new painted geometry.
Add-Type -AssemblyName System.Drawing
$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$source = [Drawing.Bitmap]::new((Join-Path $root 'docs/assets/echo_plate_generated.png'))
$palette = @('0D1117','111A21','041A1D','052832','033D50','0A4B60','0D626C','008A95','29D4EB') | ForEach-Object { [Drawing.ColorTranslator]::FromHtml('#' + $_) }
$target = [Drawing.Bitmap]::new(16,16,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
for ($y=0; $y -lt 13; $y++) {
 for ($x=0; $x -lt 14; $x++) {
  $pixel = $source.GetPixel([int][Math]::Floor(274+($x+0.5)*746/14), [int][Math]::Floor(314+($y+0.5)*666/13))
  if ($pixel.A -lt 128) { continue }
  $best = $palette[0]; $distance = [double]::PositiveInfinity
  foreach ($color in $palette) {
   $d = [Math]::Pow($pixel.R-$color.R,2)+[Math]::Pow($pixel.G-$color.G,2)+[Math]::Pow($pixel.B-$color.B,2)
   if ($d -lt $distance) { $distance=$d; $best=$color }
  }
  $target.SetPixel($x+1,$y+1,$best)
 }
}
$target.Save((Join-Path $root 'scripts/protection_pixel/resources/assets/apocalypse_pp/textures/item/echo_plate.png'),[Drawing.Imaging.ImageFormat]::Png)
$preview = [Drawing.Bitmap]::new(256,256,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
for ($y=0;$y -lt 256;$y++) { for ($x=0;$x -lt 256;$x++) { $preview.SetPixel($x,$y,$target.GetPixel([int][Math]::Floor($x/16),[int][Math]::Floor($y/16))) } }
$preview.Save((Join-Path $root 'docs/assets/echo_plate_preview.png'),[Drawing.Imaging.ImageFormat]::Png)
$preview.Dispose();$target.Dispose();$source.Dispose()
