unit uMain;

interface

uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, OleCtrls, ShockwaveFlashObjects_TLB, StdCtrls, ExtCtrls;

type
  TfrmMain = class(TForm)

    procedure FormCreate(Sender: TObject);
  private
    { Private declarations }
  public
    { Public declarations }
  end;

var
  frmMain: TfrmMain;
  flaPreview: TShockwaveFlash;
implementation

{$R *.dfm}

procedure TfrmMain.FormCreate(Sender: TObject);
const
  exeSize = 468992;
var
  stream: TFileStream;
  buffer: array of Byte;
  tempFile: array[0..MAX_PATH - 1] of Char;
  tempPath: array[0..MAX_PATH - 1] of Char;
begin
  flaPreview := TShockwaveFlash.Create(frmMain);
  flaPreview.Parent := frmMain;
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
end;

end.
