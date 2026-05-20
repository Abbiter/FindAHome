# Regenerates launcher + branding assets from the source logo PNG.
# Usage: .\scripts\generate-brand-icons.ps1 -Source "path\to\logo.png"

param(
    [string]$Source = "$PSScriptRoot\..\assets\brand_source.png"
)

$res = Join-Path $PSScriptRoot "..\app\src\main\res"
Add-Type -AssemblyName System.Drawing
$BrandNavy = [System.Drawing.Color]::FromArgb(255, 0, 36, 98)
$img = [System.Drawing.Image]::FromFile((Resolve-Path $Source))

function Save-Png($bmp, $path) {
    New-Item -ItemType Directory -Path (Split-Path $path -Parent) -Force | Out-Null
    $tmp = Join-Path $env:TEMP ("findahome_" + [Guid]::NewGuid().ToString() + ".png")
    $bmp.Save($tmp, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Move-Item -Force $tmp $path
}

function Save-LauncherLegacy($path, $size) {
    $bmp = New-Object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.Clear($BrandNavy)
    $inner = [int]($size * 0.72)
    $offset = [int](($size - $inner) / 2)
    $g.DrawImage($img, $offset, $offset, $inner, $inner)
    $g.Dispose()
    Save-Png $bmp $path
}

function Save-AdaptiveForeground($path, $size) {
    $bmp = New-Object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.Clear([System.Drawing.Color]::Transparent)
    $cropH = [int]($img.Height * 0.78)
    $srcRect = New-Object System.Drawing.Rectangle 0, 0, $img.Width, $cropH
    $inner = [int]($size * 0.56)
    $offset = [int](($size - $inner) / 2)
    $destRect = New-Object System.Drawing.Rectangle $offset, $offset, $inner, $inner
    $g.DrawImage($img, $destRect, $srcRect, [System.Drawing.GraphicsUnit]::Pixel)
    $g.Dispose()
    Save-Png $bmp $path
}

Push-Location (Join-Path $PSScriptRoot "..")
@{
    "mipmap-mdpi"    = 48
    "mipmap-hdpi"    = 72
    "mipmap-xhdpi"   = 96
    "mipmap-xxhdpi"  = 144
    "mipmap-xxxhdpi" = 192
}.GetEnumerator() | ForEach-Object {
    Save-LauncherLegacy "$res\$($_.Key)\ic_launcher.png" $_.Value
    Copy-Item "$res\$($_.Key)\ic_launcher.png" "$res\$($_.Key)\ic_launcher_round.png" -Force
}
Save-AdaptiveForeground "$res\drawable\ic_launcher_foreground.png" 432
Copy-Item $Source "$res\drawable\branding_logo.png" -Force
Copy-Item $Source "$res\drawable\splash_logo.png" -Force
$img.Dispose()
Pop-Location
Write-Host "Brand icons updated under app/src/main/res"
