# Regenerates launcher + branding assets from the source logo PNG.
# Usage: .\scripts\generate-brand-icons.ps1 -Source "path\to\logo.png"

param(
    [string]$Source = "$PSScriptRoot\..\assets\brand_source.png"
)

$res = Join-Path $PSScriptRoot "..\app\src\main\res"
Add-Type -AssemblyName System.Drawing
$img = [System.Drawing.Image]::FromFile((Resolve-Path $Source))

function Save-Scaled($path, $size) {
    New-Item -ItemType Directory -Path (Split-Path $path -Parent) -Force | Out-Null
    $bmp = New-Object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.Clear([System.Drawing.Color]::FromArgb(255, 0, 33, 98))
    $pad = [int]($size * 0.06)
    $g.DrawImage($img, $pad, $pad, $size - 2 * $pad, $size - 2 * $pad)
    $g.Dispose()
    $tmp = Join-Path $env:TEMP "findahome_icon_$size.png"
    $bmp.Save($tmp, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Move-Item -Force $tmp $path
}

Push-Location (Join-Path $PSScriptRoot "..")
@{
    "mipmap-mdpi"    = 48
    "mipmap-hdpi"    = 72
    "mipmap-xhdpi"   = 96
    "mipmap-xxhdpi"  = 144
    "mipmap-xxxhdpi" = 192
}.GetEnumerator() | ForEach-Object {
    Save-Scaled "$res\$($_.Key)\ic_launcher.png" $_.Value
    Copy-Item "$res\$($_.Key)\ic_launcher.png" "$res\$($_.Key)\ic_launcher_round.png" -Force
}
Save-Scaled "$res\drawable\ic_launcher_foreground.png" 432
Copy-Item $Source "$res\drawable\branding_logo.png" -Force
Copy-Item $Source "$res\drawable\splash_logo.png" -Force
$img.Dispose()
Pop-Location
Write-Host "Brand icons updated under app/src/main/res"
