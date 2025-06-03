# Финальное исправление NextUI props
Get-ChildItem -Path src/pages -Recurse -Include *.tsx | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    
    # Исправляем leftstartContent → startContent
    $content = $content -replace 'leftstartContent=', 'startContent='
    
    # Исправляем variant="secondary" → color="secondary" для Button
    $content = $content -replace 'variant="secondary"', 'color="secondary"'
    
    # Исправляем variant="danger" → color="danger" для Button  
    $content = $content -replace 'variant="danger"', 'color="danger"'
    
    # Убираем padding prop для Card
    $content = $content -replace '\s+padding="[^"]*"', ''
    $content = $content -replace '\s+padding="[^"]*",', ','
    
    # Убираем variant="gradient" для Card
    $content = $content -replace '\s+variant="gradient"', ''
    $content = $content -replace '\s+variant="gradient",', ','
    
    Set-Content $_.FullName $content
}

# Добавляем недостающие Icon импорты
$filesToFixImports = @(
    "src/pages/ForgotPassword.tsx",
    "src/pages/Login.tsx", 
    "src/pages/Register.tsx"
)

foreach ($file in $filesToFixImports) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw
        if ($content -notmatch "import.*Icon.*from.*@iconify/react") {
            $content = $content -replace "(import React.*\n)", "`$1import { Icon } from '@iconify/react';`n"
            Set-Content $file $content
        }
    }
} 