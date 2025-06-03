# Массовое исправление NextUI props
Get-ChildItem -Path src/pages -Recurse -Include *.tsx | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    
    # Убираем variant="glass", variant="primary" для Card
    $content = $content -replace 'variant="glass"', ''
    $content = $content -replace 'variant="primary"', 'color="primary"'
    
    # Убираем hoverable prop
    $content = $content -replace '\s+hoverable\s*', ' '
    $content = $content -replace '\s+hoverable\s*,', ','
    
    # Убираем glassmorphism prop
    $content = $content -replace '\s+glassmorphism[^,\s}]*', ''
    
    # Заменяем icon="..." на startContent={<Icon icon="..." />}
    $content = $content -replace 'icon="([^"]+)"', 'startContent={<Icon icon="$1" className="w-4 h-4" />}'
    
    # Убираем gradient prop
    $content = $content -replace '\s+gradient\s*', ' '
    $content = $content -replace '\s+gradient\s*,', ','
    
    Set-Content $_.FullName $content
} 