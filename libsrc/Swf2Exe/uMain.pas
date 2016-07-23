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
    procedure CreateWnd; override;
  end;

var
  frmMain:    TfrmMain;
  flaPreview: TMySWF;

implementation

{$R *.dfm}

procedure TMySWF.CreateWnd;
begin
  inherited;
end;

procedure TfrmMain.FormCreate(Sender: TObject);
const
  exeSize = 470016;
var
  stream: TFileStream;
  buffer: array of byte;
  tempFile: array[0..MAX_PATH - 1] of char;
  tempPath: array[0..MAX_PATH - 1] of char;
  flashVarData: TVarData;
  Width:  integer;
  Height: integer;
  scaleMode: byte;
begin
  flaPreview := TMySWF.Create(frmMain);
  flaPreview.Parent := frmMain;
  flaPreview.Anchors := [akLeft, akRight, akTop, akBottom];
  flaPreview.Align := alClient;
  GetTempPath(MAX_PATH, TempPath);
  if GetTempFileName(TempPath, PAnsiChar('ffd'), 0, TempFile) = 0 then
    raise Exception.Create('GetTempFileName API failed. ' +
      SysErrorMessage(GetLastError));
  try
    stream := TFileStream.Create(ParamStr(0), fmOpenRead);
    stream.Seek(exeSize, soBeginning);
    stream.Read(Width, 4);
    stream.Read(Height, 4);
    stream.Read(scaleMode, 1);
    SetLength(buffer, stream.Size - exeSize);
    try
      stream.Read(buffer[0], Length(buffer));
    finally
      stream.Free;
    end;
  except
    Width  := 12800;
    Height := 9600;
    scaleMode := 3;
  end;
  ClientWidth := Width div 20;
  ClientHeight := Height div 20;
  stream := TFileStream.Create(tempFile, fmOpenWrite);
  try
    stream.Write(buffer[0], Length(buffer));
  finally
    stream.Free;
  end;
  flaPreview.Movie := tempFile;
  flaPreview.ScaleMode := scaleMode;
end;

procedure TfrmMain.FormResize(Sender: TObject);
begin
  flaPreview.CreateWnd;
end;

end.
