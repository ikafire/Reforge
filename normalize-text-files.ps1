# Converts all text files in the repo to UTF-8 (no BOM) with LF line endings.
# Usage: pwsh ./normalize-text-files.ps1

$textExtensions = @(
    '.kt', '.kts', '.java',
    '.xml',
    '.gradle',
    '.properties',
    '.json',
    '.yml', '.yaml',
    '.md', '.txt',
    '.sh',
    '.pro',
    '.cfg', '.toml',
    '.editorconfig', '.gitattributes', '.gitignore'
)

$excludeDirs = @('.git', '.gradle', '.idea', 'build', 'node_modules')

$utf8NoBom = New-Object System.Text.UTF8Encoding $false
$converted = 0
$skipped = 0

Get-ChildItem -Path $PSScriptRoot -Recurse -File | ForEach-Object {
    # Skip excluded directories
    foreach ($dir in $excludeDirs) {
        if ($_.FullName -match "[\\/]$([regex]::Escape($dir))[\\/]") { return }
    }

    # Check extension (or exact filename for dotfiles)
    $match = ($textExtensions -contains $_.Extension) -or ($textExtensions -contains $_.Name)
    if (-not $match) { return }

    try {
        $bytes = [System.IO.File]::ReadAllBytes($_.FullName)

        # Skip empty files
        if ($bytes.Length -eq 0) { return }

        # Skip files that look binary (contain null bytes)
        if ($bytes -contains 0) {
            $skipped++
            return
        }

        # Detect and strip BOM if present
        $offset = 0
        if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            $offset = 3
        }

        $content = [System.Text.Encoding]::UTF8.GetString($bytes, $offset, $bytes.Length - $offset)

        # Normalize line endings: CRLF -> LF
        $normalized = $content -replace "`r`n", "`n"
        # Also catch any stray CR
        $normalized = $normalized -replace "`r", "`n"

        $newBytes = $utf8NoBom.GetBytes($normalized)

        # Only write if content actually changed
        $changed = $false
        if ($newBytes.Length -ne $bytes.Length) {
            $changed = $true
        } else {
            for ($i = 0; $i -lt $newBytes.Length; $i++) {
                if ($newBytes[$i] -ne $bytes[$i]) { $changed = $true; break }
            }
        }

        if ($changed) {
            [System.IO.File]::WriteAllBytes($_.FullName, $newBytes)
            $converted++
            Write-Host "Converted: $($_.FullName.Substring($PSScriptRoot.Length + 1))"
        }
    } catch {
        Write-Warning "Failed: $($_.FullName) - $_"
    }
}

Write-Host "`nDone. Converted: $converted file(s), Skipped (binary): $skipped file(s)."
