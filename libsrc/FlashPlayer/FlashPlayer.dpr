program FlashPlayer;

uses
  Forms,
  Windows,
  Dialogs,
  uMain in 'uMain.pas' {frmMain};

{$R *.res}

begin
  Application.Initialize;
  Application.Title := 'FFDec Flash Player';
  if(ParamCount<2) then
  begin
   ShowMessage('Wrong parameter count. This EXE is for FFDec internal use only.');
   Application.Terminate;
  end
  else
  begin
    Application.CreateForm(TfrmMain, frmMain);
  end;
  Application.Run;
end.
