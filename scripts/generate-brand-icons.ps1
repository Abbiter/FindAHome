# Regenerates launcher + branding assets from the source logo PNG.
# Usage: .\scripts\generate-brand-icons.ps1 -Source "path\to\logo.png"

param(
    [string]$Source = "$PSScriptRoot\..\assets\brand_source.png"
)

$res = Join-Path $PSScriptRoot "..\app\src\main\res"
Add-Type -AssemblyName System.Drawing
$BrandNavy = [System.Drawing.Color]::FromArgb(255, 0, 36, 98)
$img = [System.Drawing.Image]::FromFile((Resolve-Path $Source))

# Adaptive icons are masked to a circle — square side ≈ 66% / sqrt(2) ≈ 47% of canvas.
$AdaptiveSafeRatio = 0.48
# Monogram crop: top portion of logo (FAH) without small bottom tagline.
$MonogramHeightRatio = 0.66
$AdaptiveMonogramFill = 0.88

# Legacy square icons can show more of the full logo.
$LegacyFillRatio = 0.80

function Save-Png($bmp, $path) {
    New-Item -ItemType Directory -Path (Split-Path $path -Parent) -Force | Out-Null
    $tmp = Join-Path $env:TEMP ("findahome_" + [Guid]::NewGuid().ToString() + ".png")
    $bmp.Save($tmp, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Move-Item -Force $tmp $path
}

function Draw-FitRect($g, $image, $srcRect, $canvasSize, $fillRatio) {
    $maxSide = [int]($canvasSize * $fillRatio)
    $scale = [Math]::Min($maxSide / $srcRect.Width, $maxSide / $srcRect.Height)
    $w = [int]($srcRect.Width * $scale)
    $h = [int]($srcRect.Height * $scale)
    $x = [int](($canvasSize - $w) / 2)
    $y = [int](($canvasSize - $h) / 2)
    $dest = New-Object System.Drawing.Rectangle $x, $y, $w, $h
    $g.DrawImage($image, $dest, $srcRect, [System.Drawing.GraphicsUnit]::Pixel)
}

function Get-MonogramRect($image) {
    $h = [int]($image.Height * $MonogramHeightRatio)
    return New-Object System.Drawing.Rectangle 0, 0, $image.Width, $h
}

function Get-FullLogoRect($image) {
    return New-Object System.Drawing.Rectangle 0, 0, $image.Width, $image.Height
}

function Save-LauncherLegacy($path, $size) {
    $bmp = New-Object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.Clear($BrandNavy)
    Draw-FitRect $g $img (Get-FullLogoRect $img) $size $LegacyFillRatio
    $g.Dispose()
    Save-Png $bmp $path
}

function Save-AdaptiveForeground($path, $size) {
    $bmp = New-Object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.Clear([System.Drawing.Color]::Transparent)
    $mono = Get-MonogramRect $img
    Draw-FitRect $g $img $mono $size $AdaptiveMonogramFill
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
Write-Host "Brand icons updated (adaptive=FAH monogram, legacy=full logo)."
