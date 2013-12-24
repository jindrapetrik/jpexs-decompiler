unit uMain;

interface

uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, OleCtrls, ShockwaveFlashObjects_TLB, StdCtrls, ExtCtrls;

type
  TfrmMain = class(TForm)

    procedure FormCreate(Sender: TObject);
    procedure FormResize(Sender: TObject);
  private
    { Private declarations }
  public
    { Public declarations }
  end;

type
  TMySWF = class(TShockwaveFlash)
  public
    Procedure CreateWnd; override;
  end;

var
  frmMain: TfrmMain;
  flaPreview: TMySWF;
implementation

{$R *.dfm}

Procedure TMySWF.CreateWnd;
begin
  inherited;
end;

procedure TfrmMain.FormCreate(Sender: TObject);
const
  exeSize = 469504;
var
  stream: TFileStream;
  buffer: array of Byte;
  tempFile: array[0..MAX_PATH - 1] of Char;
  tempPath: array[0..MAX_PATH - 1] of Char;
  flashVarData : TVarData;
begin
  flaPreview := TMySWF.Create(frmMain);
  flaPreview.Parent := frmMain;
  flaPreview.Anchors := [akLeft, akRight, akTop, akBottom];
  flaPreview.Align := alClient;
  GetTempPath(MAX_PATH, TempPath);
  if GetTempFileName(TempPath, PAnsiChar('ffd'), 0, TempFile) = 0 then
    raise Exception.Create(
      'GetTempFileName API failed. ' + SysErrorMessage(GetLastError)
    );
  stream := TFileStream.Create(ParamStr(0), fmOpenRead);
  stream.Seek(exeSize, soBeginning);
  SetLength(buffer, stream.Size - exeSize);
  try
    stream.Read(buffer[0], Length(buffer));
  finally
    stream.Free;
  end;
  stream := TFileStream.Create(ParamStr(0), fmOpenRead);
  stream.Seek(exeSize, soBeginning);
  stream := TFileStream.Create(tempFile, fmOpenWrite);
  try
    stream.Write(buffer[0], Length(buffer));
  finally
    stream.Free;
  end;
  flaPreview.Movie := tempFile;
  flaPreview.ScaleMode := 3;
end;

procedure TfrmMain.FormResize(Sender: TObject);
begin
  flaPreview.CreateWnd;
end;

end.
