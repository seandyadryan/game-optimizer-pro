# Regenerate Play listing artwork from the app's original lightning mark.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$output = Join-Path $PSScriptRoot '../docs/store-assets'
New-Item -ItemType Directory -Force -Path $output | Out-Null
$dark = [Drawing.ColorTranslator]::FromHtml('#0C1019')
$panel = [Drawing.ColorTranslator]::FromHtml('#171E2B')
$lime = [Drawing.ColorTranslator]::FromHtml('#B9F66B')
$muted = [Drawing.ColorTranslator]::FromHtml('#A3AEC2')
$white = [Drawing.Color]::FromArgb(245, 247, 252)

function Draw-Mark($graphics, [float]$x, [float]$y, [float]$scale) {
    $points = @(@(58,22), @(33,58), @(49,58), @(44,86), @(76,45), @(58,45), @(66,22))
    [Drawing.PointF[]]$polygon = $points | ForEach-Object {
        [Drawing.PointF]::new($x + $_[0] * $scale, $y + $_[1] * $scale)
    }
    $brush = [Drawing.SolidBrush]::new($lime)
    $graphics.FillPolygon($brush, $polygon)
    $brush.Dispose()
}

function Draw-RoundedPanel($graphics, [Drawing.Color]$color, [float]$x, [float]$y, [float]$width, [float]$height, [float]$radius) {
    $path = [Drawing.Drawing2D.GraphicsPath]::new()
    $diameter = $radius * 2
    $path.AddArc($x, $y, $diameter, $diameter, 180, 90)
    $path.AddArc(($x + $width - $diameter), $y, $diameter, $diameter, 270, 90)
    $path.AddArc(($x + $width - $diameter), ($y + $height - $diameter), $diameter, $diameter, 0, 90)
    $path.AddArc($x, ($y + $height - $diameter), $diameter, $diameter, 90, 90)
    $path.CloseFigure()
    $brush = [Drawing.SolidBrush]::new($color)
    $graphics.FillPath($brush, $path)
    $brush.Dispose()
    $path.Dispose()
}

function Draw-FittedText($graphics, [string]$text, [float]$x, [float]$y, [float]$maxWidth, [float]$maxHeight, [float]$fontSize, [float]$minimumSize, [Drawing.FontStyle]$style, [Drawing.Brush]$brush) {
    $font = $null
    do {
        if ($font) { $font.Dispose() }
        $font = [Drawing.Font]::new('Arial', $fontSize, $style, [Drawing.GraphicsUnit]::Pixel)
        $size = $graphics.MeasureString($text, $font)
        if ($size.Width -le $maxWidth -and $size.Height -le $maxHeight) { break }
        $fontSize -= 1
    } while ($fontSize -ge $minimumSize)
    if ($size.Width -gt $maxWidth -or $size.Height -gt $maxHeight) {
        $font.Dispose()
        throw "Store artwork text does not fit: $text"
    }
    $graphics.DrawString($text, $font, $brush, $x, $y)
    $font.Dispose()
}

# 512 x 512 opaque square app icon. No text, badge, or transparency.
$icon = [Drawing.Bitmap]::new(512, 512)
$graphics = [Drawing.Graphics]::FromImage($icon)
$graphics.SmoothingMode = [Drawing.Drawing2D.SmoothingMode]::AntiAlias
$graphics.Clear($dark)
Draw-Mark $graphics 22 24 4.3
$icon.Save((Join-Path $output 'play-icon-512.png'), [Drawing.Imaging.ImageFormat]::Png)
$graphics.Dispose()
$icon.Dispose()

function New-FeatureGraphic([string]$filename, [string]$tagline, [string[]]$features) {
    $bitmap = [Drawing.Bitmap]::new(1024, 500)
    $graphics = [Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.TextRenderingHint = [Drawing.Text.TextRenderingHint]::AntiAliasGridFit

    $background = [Drawing.Drawing2D.LinearGradientBrush]::new(
        [Drawing.Rectangle]::new(0, 0, 1024, 500),
        [Drawing.ColorTranslator]::FromHtml('#121A24'),
        $dark,
        [Drawing.Drawing2D.LinearGradientMode]::ForwardDiagonal
    )
    $graphics.FillRectangle($background, 0, 0, 1024, 500)
    $background.Dispose()

    # Subtle circular accents add depth without competing with the copy.
    $outline = [Drawing.Pen]::new([Drawing.Color]::FromArgb(24, $lime), 2)
    $graphics.DrawEllipse($outline, 795, -175, 430, 430)
    $graphics.DrawEllipse($outline, 850, -120, 320, 320)
    $outline.Dispose()

    Draw-RoundedPanel $graphics $panel 54 95 305 310 48
    Draw-Mark $graphics 31 77 3.2

    $brandBrush = [Drawing.SolidBrush]::new($white)
    $limeBrush = [Drawing.SolidBrush]::new($lime)
    $mutedBrush = [Drawing.SolidBrush]::new($muted)
    $graphics.DrawString('GAME OPTIMIZER PRO',
        [Drawing.Font]::new('Arial', 38, [Drawing.FontStyle]::Bold, [Drawing.GraphicsUnit]::Pixel),
        $brandBrush, 414, 115)
    Draw-FittedText $graphics $tagline 416 192 570 70 31 25 ([Drawing.FontStyle]::Regular) $limeBrush

    $featureXs = @(417, 612, 807)
    for ($i = 0; $i -lt $features.Length; $i++) {
        $graphics.FillEllipse($limeBrush, $featureXs[$i], 315, 10, 10)
        Draw-FittedText $graphics $features[$i] ($featureXs[$i] + 17) 308 170 30 17 13 ([Drawing.FontStyle]::Bold) $mutedBrush
    }

    $graphics.DrawLine([Drawing.Pen]::new([Drawing.Color]::FromArgb(65, $lime), 2), 414, 276, 970, 276)
    $bitmap.Save((Join-Path $output $filename), [Drawing.Imaging.ImageFormat]::Png)
    @($graphics, $bitmap, $brandBrush, $limeBrush, $mutedBrush) | ForEach-Object { $_.Dispose() }
}

New-FeatureGraphic 'feature-graphic-1024x500.png' 'READY FOR YOUR NEXT GAME' @('DEVICE INFO', 'GAME LIBRARY', 'SESSION LOG')
New-FeatureGraphic 'feature-graphic-id-1024x500.png' 'SIAPKAN DIRI UNTUK BERMAIN' @('INFO PERANGKAT', 'PUSTAKA GAME', 'CATATAN SESI')
Write-Output "Play listing graphics saved to $output"
