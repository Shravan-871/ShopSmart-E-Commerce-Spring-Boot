param(
    [Parameter(Mandatory=$true)]
    [string]$Message,

    [Parameter(Mandatory=$false)]
    [string]$Description = ""
)

$repo = "C:\Users\shett\3D Objects\Github\ShopSmart-E-Commerce-Spring-Boot"
Set-Location $repo

# Stage all changes
git add -A

# Check if there is anything to commit
$status = git status --porcelain
if (-not $status) {
    Write-Host "Nothing to commit, working tree clean." -ForegroundColor Yellow
    exit 0
}

# Print changed files
Write-Host ""
Write-Host "Changed / Modified / Created Files" -ForegroundColor Cyan
Write-Host "-----------------------------------" -ForegroundColor Cyan

$status -split "`n" | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq "") { return }

    $code = $line.Substring(0, 2).Trim()
    $file = $line.Substring(3)

    switch ($code) {
        "A"  { Write-Host "  [ADDED]    $file" -ForegroundColor Green }
        "M"  { Write-Host "  [MODIFIED] $file" -ForegroundColor Yellow }
        "D"  { Write-Host "  [DELETED]  $file" -ForegroundColor Red }
        "R"  { Write-Host "  [RENAMED]  $file" -ForegroundColor Magenta }
        "??" { Write-Host "  [NEW]      $file" -ForegroundColor Green }
        default { Write-Host "  [$code]  $file" -ForegroundColor White }
    }
}

Write-Host ""

# Commit
if ($Description -ne "") {
    git commit -m $Message -m $Description
} else {
    git commit -m $Message
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "Commit failed." -ForegroundColor Red
    exit 1
}

# Push
git push

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "Pushed successfully." -ForegroundColor Green
} else {
    Write-Host "Push failed." -ForegroundColor Red
    exit 1
}
