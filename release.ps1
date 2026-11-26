param(
    [string]$Notes = "Updated features and latest opportunities from laptop build."
)

$gradleFile = "app\build.gradle.kts"
$content = Get-Content $gradleFile -Raw

if ($content -match 'versionCode\s*=\s*(\d+)') {
    $oldCode = [int]$matches[1]
    $newCode = $oldCode + 1
    $newName = "1.$newCode"
    $content = $content -replace 'versionCode\s*=\s*\d+', "versionCode = $newCode"
    $content = $content -replace 'versionName\s*=\s*"[^"]+"', "versionName = `"$newName`""
    Set-Content $gradleFile $content

    .\gradlew assembleDebug
    New-Item -ItemType Directory -Force -Path releases | Out-Null
    Copy-Item "app\build\outputs\apk\debug\app-debug.apk" "releases\ScholarTrack-latest.apk" -Force

    $versionJson = @{
        versionCode = $newCode
        versionName = $newName
        notes = $Notes
        apkUrl = "https://github.com/bramo2-cpu/scholartrackapp/raw/main/releases/ScholarTrack-latest.apk"
    } | ConvertTo-Json
    Set-Content "version.json" $versionJson

    git add .
    git commit -m "Release v$newName : $Notes"
    git push origin main
    Write-Host "Released v$newName! Your phone will now prompt to update when opened." -ForegroundColor Green
} else {
    Write-Host "Could not find versionCode in app\build.gradle.kts" -ForegroundColor Red
}
