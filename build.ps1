# Rebuilds touhou_spell from this source tree against the *installed* 1.20.1 Forge runtime.
#
# Why not a normal Forge MDK? The sources in src/main/java are the decompiled PRODUCTION jar,
# so they still use the SRG member names (m_41720_, f_19802_, ...). They therefore have to be
# compiled against the production (SRG) classpath, not against a dev workspace that uses the
# official Mojang names. This script reproduces the exact classpath the game uses.
#
# Usage:  powershell -ExecutionPolicy Bypass -File build.ps1
#
param(
    [string]$McDir = "$env:APPDATA\.minecraft",
    [string]$Instance = "1.20.1-Forge-project"
)

$ErrorActionPreference = 'Stop'
$proj   = Split-Path -Parent $MyInvocation.MyCommand.Path
$build  = Join-Path $proj 'build'
$srcDir = Join-Path $proj 'src\main\java'
$resDir = Join-Path $proj 'src\main\resources'
$libDir = Join-Path $build 'libs'
$outDir = Join-Path $build 'classes'
$version = '1.0.0'
$jarname = "touhou_spell-$version.jar"

New-Item -ItemType Directory -Force $build | Out-Null
Remove-Item -Recurse -Force $libDir, $outDir -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $libDir, $outDir | Out-Null

# ---------------------------------------------------------------- 1. classpath
$forgeVersion = (Get-ChildItem -Directory (Join-Path $McDir 'libraries\net\minecraftforge\forge') |
    Where-Object { $_.Name -like '1.20.1-*' } |
    Sort-Object { [version]($_.Name -replace '^1\.20\.1-', '') } -Descending | Select-Object -First 1).Name
if (-not $forgeVersion) { throw "No Forge 1.20.1 install found under $McDir\libraries" }

$forgeClient = Join-Path $McDir "libraries\net\minecraftforge\forge\$forgeVersion\forge-$forgeVersion-client.jar"
$srg = Get-ChildItem -Recurse -File -Filter 'client-1.20.1-*-srg.jar' (Join-Path $McDir 'libraries\net\minecraft\client') |
    Select-Object -First 1 -ExpandProperty FullName
if (-not (Test-Path $forgeClient)) { throw "Missing $forgeClient" }
if (-not $srg) { throw "Missing the remapped client jar (client-*-srg.jar)" }

# Forge client jar holds the patched MC classes (getCapability, ResourceLocation.fromNamespaceAndPath, ...)
# and must come first; the srg jar supplies the rest of Minecraft.
$oldForgeArtifact = '\\net\\minecraftforge\\(forge|fmlcore|fmlloader|fmlearlydisplay|javafmllanguage|lowcodelanguage|mclanguage)\\'
$libs = Get-ChildItem -Recurse -File -Filter *.jar (Join-Path $McDir 'libraries') |
    Where-Object { $_.FullName -notmatch '\\net\\minecraft\\' } |
    Where-Object { $_.FullName -notmatch '\\net\\neoforged\\' } |
    Where-Object { $_.FullName -ne $forgeClient } |
    Where-Object { -not ($_.FullName -match $oldForgeArtifact -and $_.FullName -notmatch [regex]::Escape($forgeVersion)) } |
    ForEach-Object FullName

# JarJar'ed libraries of the installed mods (l2library, fastprojectileapi, ...) are compile deps too.
$nested = Join-Path $build 'nested'
Remove-Item -Recurse -Force $nested -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $nested | Out-Null
foreach ($mod in Get-ChildItem -File -Filter *.jar (Join-Path $McDir "versions\$Instance\mods")) {
    if ($mod.Name -like 'touhou_spell-*') { continue }
    $tmp = Join-Path $nested '_tmp'
    Remove-Item -Recurse -Force $tmp -ErrorAction SilentlyContinue
    New-Item -ItemType Directory -Force $tmp | Out-Null
    Push-Location $tmp
    & jar xf $mod.FullName 'META-INF/jarjar' 2>$null
    Pop-Location
    if (Test-Path (Join-Path $tmp 'META-INF\jarjar')) {
        Get-ChildItem -File (Join-Path $tmp 'META-INF\jarjar') | ForEach-Object {
            Copy-Item $_.FullName (Join-Path $nested $_.Name) -Force
        }
    }
    Remove-Item -Recurse -Force $tmp
}
$nestedJars = Get-ChildItem -File -Filter *.jar $nested | ForEach-Object FullName

# Compile-time only: org.jetbrains.annotations is not shipped in .minecraft/libraries,
# but the Gradle cache of this machine has it (downloading is not required).
$annotations = Join-Path $build 'annotations-24.0.1.jar'
if (-not (Test-Path $annotations)) {
    $cached = Get-ChildItem -Recurse -File -Filter 'annotations-24.0.1.jar' "$env:USERPROFILE\.gradle\caches\modules-2" -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty FullName
    if ($cached) { Copy-Item $cached $annotations -Force }
    else { Write-Warning "org.jetbrains:annotations not found - compilation may fail on @NotNull." }
}

$modJars = Get-ChildItem -File -Filter *.jar (Join-Path $McDir "versions\$Instance\mods") |
    Where-Object { $_.Name -notlike 'touhou_spell-*' } | ForEach-Object FullName

$cp = @($forgeClient, $srg) + @($libs) + @($modJars) + @($nestedJars)
if (Test-Path $annotations) { $cp += $annotations }

# ---------------------------------------------------------------- 2. compile
$sources = Get-ChildItem -Recurse -File -Filter *.java $srcDir | ForEach-Object FullName
$sourcesFile = Join-Path $build 'sources.txt'
# javac parses @argfiles shell-style: backslashes are escapes, so emit forward slashes.
$argLines = @(
    ('-cp "' + (($cp | ForEach-Object { $_ -replace '\\', '/' }) -join ';') + '"'),
    ('-d "' + ($outDir -replace '\\', '/') + '"'),
    '-encoding UTF-8',
    '-proc:none',
    '-nowarn'
) + ($sources | ForEach-Object { '"' + ($_ -replace '\\', '/') + '"' })
[System.IO.File]::WriteAllLines($sourcesFile, $argLines, (New-Object System.Text.UTF8Encoding($false)))

$prevEap = $ErrorActionPreference
$ErrorActionPreference = 'Continue'   # javac writes its notes to stderr; that is not a failure
& javac "@$sourcesFile" *> (Join-Path $build 'javac.log')
$javacExit = $LASTEXITCODE
$ErrorActionPreference = $prevEap
if ($javacExit -ne 0) {
    Get-Content (Join-Path $build 'javac.log') -Tail 40
    throw "javac failed (see build\javac.log)"
}

# ---------------------------------------------------------------- 3. package
$manifest = @"
Manifest-Version: 1.0
Specification-Title: touhou_spell
Specification-Vendor: zxyume
Specification-Version: 1
Implementation-Title: touhou_spell
Implementation-Version: $version
Implementation-Vendor: zxyume
MixinConfigs: touhou_spell.mixins.json

"@
$manifestFile = Join-Path $build 'MANIFEST.MF'
[System.IO.File]::WriteAllText($manifestFile, $manifest, (New-Object System.Text.UTF8Encoding($false)))

$jar = Join-Path $build $jarname
Remove-Item -Force $jar -ErrorAction SilentlyContinue
Push-Location $build
& jar cfm $jar MANIFEST.MF -C $outDir . -C $resDir .
Pop-Location
if ($LASTEXITCODE -ne 0) { throw "jar failed" }

Write-Host "OK -> $jar"

