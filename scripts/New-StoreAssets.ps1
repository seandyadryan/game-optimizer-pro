# Regenerate listing graphics from the application's original lightning mark.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$output = Join-Path $PSScriptRoot '../docs/store-assets'
New-Item -ItemType Directory -Force -Path $output | Out-Null
$dark = [Drawing.ColorTranslator]::FromHtml('#0C1019')
$lime = [Drawing.ColorTranslator]::FromHtml('#B9F66B')
$muted = [Drawing.ColorTranslator]::FromHtml('#A3AEC2')

function Draw-Mark($graphics, [float]$x, [float]$y, [float]$scale) {
    $points = @(@(58,22), @(33,58), @(49,58), @(44,86), @(76,45), @(58,45), @(66,22))
    [Drawing.PointF[]]$polygon = $points | ForEach-Object { [Drawing.PointF]::new($x + $_[0] * $scale, $y + $_[1] * $scale) }
    $brush = [Drawing.SolidBrush]::new($lime)
    $graphics.FillPolygon($brush, $polygon)
    $brush.Dispose()
}

$icon = [Drawing.Bitmap]::new(512, 512)
$graphics = [Drawing.Graphics]::FromImage($icon)
$graphics.SmoothingMode = [Drawing.Drawing2D.SmoothingMode]::AntiAlias
$graphics.Clear($dark)
Draw-Mark $graphics 0 0 (512.0 / 108)
$icon.Save((Join-Path $output 'play-icon-512.png'), [Drawing.Imaging.ImageFormat]::Png)
$graphics.Dispose()
$icon.Dispose()

$banner = [Drawing.Bitmap]::new(1024, 500)
$graphics = [Drawing.Graphics]::FromImage($banner)
$graphics.SmoothingMode = [Drawing.Drawing2D.SmoothingMode]::AntiAlias
$graphics.TextRenderingHint = [Drawing.Text.TextRenderingHint]::AntiAliasGridFit
$graphics.Clear($dark)
$pen = [Drawing.Pen]::new([Drawing.ColorTranslator]::FromHtml('#202B26'), 1)
for ($x = 0; $x -le 1024; $x += 40) { $graphics.DrawLine($pen, $x, 0, $x, 500) }
for ($y = 0; $y -le 500; $y += 40) { $graphics.DrawLine($pen, 0, $y, 1024, $y) }
$graphics.FillRectangle([Drawing.Brushes]::Black, 405, 0, 619, 500)
Draw-Mark $graphics -20 18 4.3
$whiteBrush = [Drawing.SolidBrush]::new([Drawing.Color]::White)
$limeBrush = [Drawing.SolidBrush]::new($lime)
$mutedBrush = [Drawing.SolidBrush]::new($muted)
$label = [Drawing.Font]::new('Arial', 17, [Drawing.FontStyle]::Bold, [Drawing.GraphicsUnit]::Pixel)
$headline = [Drawing.Font]::new('Arial', 52, [Drawing.FontStyle]::Bold, [Drawing.GraphicsUnit]::Pixel)
$body = [Drawing.Font]::new('Arial', 20, [Drawing.FontStyle]::Regular, [Drawing.GraphicsUnit]::Pixel)
$graphics.DrawString('GAME OPTIMIZER PRO', $label, $limeBrush, 454, 80)
$graphics.DrawString("Siapkan sesi`nterbaikmu.", $headline, $whiteBrush, 450, 150)
$graphics.DrawString("Pantau perangkat. Kelola game.`nTanpa iklan. Tanpa pelacak.", $body, $mutedBrush, 454, 335)
$banner.Save((Join-Path $output 'feature-graphic-1024x500.png'), [Drawing.Imaging.ImageFormat]::Png)
@($graphics, $banner, $pen, $whiteBrush, $limeBrush, $mutedBrush, $label, $headline, $body) | ForEach-Object { $_.Dispose() }
Write-Output "Listing graphics saved to $output"
