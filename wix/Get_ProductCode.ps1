$msiPath = "wix\bin\Release\FFDec.msi"
$installer = New-Object -ComObject WindowsInstaller.Installer
$db = $installer.OpenDatabase($msiPath, 0)
$view = $db.OpenView("SELECT `Value` FROM `Property` WHERE `Property`='ProductCode'")
$view.Execute()
$rec = $view.Fetch()
$rec.StringData(1)