# Regenerates launcher + branding assets from the source logo PNG.
# Usage: .\scripts\generate-brand-icons.ps1 -Source "path\to\logo.png"

param(
    [string]$Source = "$PSScriptRoot\..\assets\brand_source.png"
)

$res = Join-Path $PSScriptRoot "..\app\src\main\res"
Add-Type -AssemblyName System.Drawing
$BrandNavy = [System.Drawing.Color]::FromArgb(255, 0, 36, 98)
$img = [System.Drawing.Image]::FromFile((Resolve-Path $Source))

# Adaptive icon: circle mask — keep logo near ~46% of canvas height.
$AdaptiveFillRatio = 0.52
# Legacy launcher squares.
$LegacyFillRatio = 0.78

function Save-Png($bmp, $path) {
    New-Item -ItemType Directory -Path (Split-Path $path -Parent) -Force | Out-Null
    $tmp = Join-Path $env:TEMP ("findahome_" + [Guid]::NewGuid().ToString() + ".png")
    $bmp.Save($tmp, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Move-Item -Force $tmp $path
}

function Test-IsBrandBlue([System.Drawing.Color]$c) {
    # Navy card (#002462) and nearby anti-aliased blues.
    return ($c.B -ge 70 -and $c.R -le 55 -and $c.G -le 70)
}

function Test-IsLogoInk([System.Drawing.Color]$c) {
    # Black "H" and dark strokes — remap to white for navy launcher background.
    return ($c.R -le 70 -and $c.G -le 70 -and $c.B -le 70 -and -not (Test-IsBrandBlue $c))
}

function Copy-ToTransparentForeground([System.Drawing.Bitmap]$source, [System.Drawing.Rectangle]$srcRect) {
    $bmp = New-Object System.Drawing.Bitmap $srcRect.Width, $srcRect.Height
    for ($y = 0; $y -lt $srcRect.Height; $y++) {
        for ($x = 0; $x -lt $srcRect.Width; $x++) {
            $c = $source.GetPixel($srcRect.X + $x, $srcRect.Y + $y)
            if (Test-IsBrandBlue $c) {
                $bmp.SetPixel($x, $y, [System.Drawing.Color]::Transparent)
            } elseif (Test-IsLogoInk $c) {
                $bmp.SetPixel($x, $y, [System.Drawing.Color]::White)
            } else {
                $bmp.SetPixel($x, $y, $c)
            }
        }
    }
    return $bmp
}

function Get-BlueCardBitmap([System.Drawing.Bitmap]$source) {
    $w = $source.Width
    $h = $source.Height
    $minX = $w
    $minY = $h
    $maxX = 0
    $maxY = 0
    for ($y = 0; $y -lt $h; $y++) {
        for ($x = 0; $x -lt $w; $x++) {
            if (Test-IsBrandBlue ($source.GetPixel($x, $y))) {
                if ($x -lt $minX) { $minX = $x }
                if ($y -lt $minY) { $minY = $y }
                if ($x -gt $maxX) { $maxX = $x }
                if ($y -gt $maxY) { $maxY = $y }
            }
        }
    }
    if ($maxX -le $minX -or $maxY -le $minY) {
        return $source
    }
    $rect = New-Object System.Drawing.Rectangle $minX, $minY, ($maxX - $minX + 1), ($maxY - $minY + 1)
    $card = New-Object System.Drawing.Bitmap $rect.Width, $rect.Height
    $g = [System.Drawing.Graphics]::FromImage($card)
    $g.DrawImage($source, 0, 0, $rect, [System.Drawing.GraphicsUnit]::Pixel)
    $g.Dispose()
    return $card
}

# Full blue card: FAH + "Find A Home" (no outer black mat).
function Get-WordmarkRect([System.Drawing.Bitmap]$bmp) {
    return New-Object System.Drawing.Rectangle 0, 0, $bmp.Width, $bmp.Height
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

function Save-LauncherLegacy($path, $size, $logo, $srcRect) {
    $bmp = New-Object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.Clear($BrandNavy)
    Draw-FitRect $g $logo $srcRect $size $LegacyFillRatio
    $g.Dispose()
    Save-Png $bmp $path
}

function Save-AdaptiveForeground($path, $size, $logo, $srcRect) {
    $cutout = Copy-ToTransparentForeground $logo $srcRect
    $bmp = New-Object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.Clear([System.Drawing.Color]::Transparent)
    $full = New-Object System.Drawing.Rectangle 0, 0, $cutout.Width, $cutout.Height
    Draw-FitRect $g $cutout $full $size $AdaptiveFillRatio
    $g.Dispose()
    $cutout.Dispose()
    Save-Png $bmp $path
}

Push-Location (Join-Path $PSScriptRoot "..")
$blueCard = Get-BlueCardBitmap $img
$wordmark = Get-WordmarkRect $blueCard

@{
    "mipmap-mdpi"    = 48
    "mipmap-hdpi"    = 72
    "mipmap-xhdpi"   = 96
    "mipmap-xxhdpi"  = 144
    "mipmap-xxxhdpi" = 192
}.GetEnumerator() | ForEach-Object {
    Save-LauncherLegacy "$res\$($_.Key)\ic_launcher.png" $_.Value $blueCard $wordmark
    Copy-Item "$res\$($_.Key)\ic_launcher.png" "$res\$($_.Key)\ic_launcher_round.png" -Force
}
Save-AdaptiveForeground "$res\drawable\ic_launcher_foreground.png" 432 $blueCard $wordmark

# In-app / splash still use the full supplied artwork.
Copy-Item $Source "$res\drawable\branding_logo.png" -Force
Copy-Item $Source "$res\drawable\splash_logo.png" -Force

$blueCard.Dispose()
$img.Dispose()
Pop-Location
Write-Host "Icons updated: no black frame, wordmark (FAH + Find A Home) on navy."
