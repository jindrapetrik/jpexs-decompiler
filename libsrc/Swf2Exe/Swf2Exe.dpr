program Swf2Exe;

uses
  Forms,
  Windows,
  Dialogs,
  uMain in 'uMain.pas' {frmMain};

{$R *.res}

begin
  Application.Initialize;
  Application.Title := 'FFDec Flash Player';
  Application.CreateForm(TfrmMain, frmMain);
  Application.Run;
end.
