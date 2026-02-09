$exts = @('swf', 'spl', 'gfx', 'swt', 'swc')
#$exts = @('xyz')

$template = Get-Content -Path '.\ContextMenuExtensionTemplate.xml' -Raw -Encoding UTF8
$obal     = Get-Content -Path '.\Product.wxs'     -Raw -Encoding UTF8

$blocks = foreach ($ext in $exts) {
    $extLower = $ext.ToLowerInvariant() # "swf"
    $extUpper = $ext.ToUpperInvariant() # "SWF"
    $extFirstUpper = $ext.Substring(0,1).ToUpperInvariant() + $ext.Substring(1) # "Swf"

    $t = $template
    $t = $t -creplace '\{\{ext\}\}', $extLower
    $t = $t -creplace '\{\{EXT\}\}', $extUpper
    $t = $t -creplace '\{\{Ext\}\}', $extFirstUpper

    $t = $t -creplace '\{\{GUID_\d+\}\}', {
        [guid]::NewGuid().ToString("D").ToUpper()
    }

    $t
}

$insertText = ($blocks -join "`r`n")

$pattern = '(?s)(<!-- EXTENSION TEMPLATE HERE: -->)(.*?)(<!-- /EXTENSION TEMPLATE HERE: -->)'
if ($obal -notmatch $pattern) { throw 'Section <!-- EXTENSION TEMPLATE HERE: -->...<!-- /EXTENSION TEMPLATE HERE: --> not found.' }

$result = [regex]::Replace($obal, $pattern, {
    param($m)
    $open  = $m.Groups[1].Value
    $inner = $m.Groups[2].Value
    $close = $m.Groups[3].Value

    return $open + "`r`n" + $insertText + "`r`n" + $close

})

Set-Content -Path '.\Product.wxs' -Value $result -Encoding UTF8