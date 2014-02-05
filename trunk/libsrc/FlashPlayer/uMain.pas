unit uMain;

interface

uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, OleCtrls, ShockwaveFlashObjects_TLB, StdCtrls, ExtCtrls;

type
  TfrmMain = class(TForm)
    tmrWatchDog: TTimer;

    procedure FormActivate(Sender: TObject);
    procedure FormCreate(Sender: TObject);
    procedure FormDestroy(Sender: TObject);
    procedure tmrWatchDogTimer(Sender: TObject);
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

type
  TPipeThread = class(TThread)
  protected
    flashFile: string;
    w:         integer;
    h:         integer;
    bgColor:   TColor;
    procedure Execute; override;
    procedure displaySWF;
    procedure freeSWF;
    procedure setPos;
    procedure setBGColor;
  end;

  TBuf = array[0..255] of byte;

var
  frmMain:    TfrmMain;
  k:          string;
  t:          TPipeThread;
  flaPreview: TMySWF;
  target:     HWND = 0;

implementation

{$R *.dfm}



procedure TMySWF.CreateWnd;
begin
  inherited;
end;

function arrToStr(k: TBuf; len: integer): string;
var
  s: string;
  i: integer;
begin
  s := '';
  for i := 0 to len - 1 do
  begin
    if k[i] = 0 then
      break;
    s := s + '' + chr(k[i]);
  end;
  Result := s;
end;

procedure ReadPipe(pipe: cardinal; var buffer: TBuf; bytesToRead: cardinal);
var
  numBytesRead: DWORD;
  readResult: longbool;
begin
  numBytesRead := 0;
  readResult := ReadFile(pipe, buffer, bytesToRead, numBytesRead, nil);
  if (not readResult) or (numBytesRead <> bytesToRead) then
  begin
    Application.Terminate;
  end;
end;

procedure WritePipe(pipe: cardinal; var buffer: TBuf; bytesToWrite: cardinal);
var
  written: cardinal;
  writeResult: longbool;
begin
  written := 0;
  writeResult := WriteFile(pipe, buffer, bytesToWrite, written, nil);
  if (not writeResult) or (written <> bytesToWrite) then
  begin
    Application.Terminate;
  end;
end;

procedure TPipeThread.freeSWF();
begin
  if Assigned(flaPreview) then
  begin
    try
      flaPreview.Stop;
      flaPreview.Movie := '';
      flaPreview.Free;
      flaPreview := nil;
    except
      on E: Exception do
      begin
      end;
    end;
  end;
end;

procedure TPipeThread.displaySWF();
begin
  Windows.SetParent(frmMain.Handle, target);
  freeSWF();
  flaPreview := TMySWF.Create(frmMain);
  flaPreview.Left := 0;
  flaPreview.Top := 0;
  flaPreview.Width := self.w;
  flaPreview.Height := self.h;
  frmMain.Caption := 'set movie:' + flashFile;
  flaPreview.Parent := frmMain;
  flaPreview.Movie := flashFile;
  flaPreview.SetFocus;
end;

procedure TPipeThread.setBGColor();
begin
  frmMain.Color := self.bgColor;
end;


procedure TPipeThread.setPos();
begin
  SetWindowPos(frmMain.Handle, 0, 0, 0, self.w, self.h, SWP_SHOWWINDOW);
  flaPreview.Left := 0;
  flaPreview.Top  := 0;
  flaPreview.Width := self.w;
  flaPreview.Height := self.h;
  flaPreview.CreateWnd;
  //displaySWF();
end;

procedure TPipeThread.Execute();
var
  f: Textfile;
  pipe: cardinal;
  buffer: TBuf;
  pipename: PAnsiChar;
  len: integer;
  cmd: integer;
  val: cardinal;
  vals: string;
  vars: string;

const
  CMD_PLAY  = 1;
  CMD_RESIZE = 2;
  CMD_BGCOLOR = 3;
  CMD_CURRENT_FRAME = 4;
  CMD_TOTAL_FRAMES = 5;
  CMD_PAUSE = 6;
  CMD_RESUME = 7;
  CMD_PLAYING = 8;
  CMD_REWIND = 9;
  CMD_GOTO  = 10;
  CMD_CALL  = 11;
  CMD_GETVARIABLE = 12;
  CMD_SETVARIABLE = 13;
begin

  AssignFile(f, 'c:\log\pipelog.txt');
  if FileExists('c:\log\pipelog.txt') = False then
    Rewrite(f)
  else
  begin
    Append(f);
  end;

  try
    pipename := PAnsiChar('\\.\\pipe\ffdec_flashplayer_' + ParamStr(1));
    begin
      pipe := CreateFile(pipename, GENERIC_READ or GENERIC_WRITE,
        FILE_SHARE_READ or FILE_SHARE_WRITE, nil, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, 0);

      repeat
        cmd := 0;
        try
          ReadPipe(pipe, buffer, 1);
          cmd := buffer[0];
          case cmd of
            CMD_PLAY:
            begin
              ReadPipe(pipe, buffer, 1);
              len := buffer[0];
              ReadPipe(pipe, buffer, len);
              self.flashFile := arrToStr(buffer, len);
              try
                Synchronize(displaySWF);
              except
                on E: Exception do
                begin
                  freeSWF();
                  Writeln(f, 'CMD_PLAY error: ' + E.Message);
                  Flush(f);
                end;
              end;
            end;
            CMD_RESIZE:
            begin
              ReadPipe(pipe, buffer, 4);
              self.w := buffer[0] * 256 + buffer[1];
              self.h := buffer[2] * 256 + buffer[3];
              Synchronize(setPos);
            end;
            CMD_BGCOLOR:
            begin
              ReadPipe(pipe, buffer, 3);
              self.bgColor := RGB(buffer[0], buffer[1], buffer[2]);
              Synchronize(setBGColor);
            end;
            CMD_CURRENT_FRAME:
            begin
              val := 0;
              try
                if flaPreview.ReadyState = 4 then
                  val := flaPreview.CurrentFrame
              except
                on E: Exception do
                begin
                  freeSWF();
                  Writeln(f, 'CMD_CURRENT_FRAME error: ' + E.Message);
                  Flush(f);
                end;
              end;
              buffer[0] := (val shr 8) mod 256;
              buffer[1] := val mod 256;
              WritePipe(pipe, buffer, 2);
            end;
            CMD_TOTAL_FRAMES:
            begin
              val := 0;
              try
                if flaPreview.ReadyState = 4 then
                  val := flaPreview.TotalFrames
              except
                on E: Exception do
                begin
                  freeSWF();
                  Writeln(f, 'CMD_TOTAL_FRAMES error: ' + E.Message);
                  Flush(f);
                end;
              end;
              buffer[0] := (val shr 8) mod 256;
              buffer[1] := val mod 256;
              WritePipe(pipe, buffer, 2);
            end;
            CMD_PAUSE:
            begin
              try
                flaPreview.Stop;
              except
                on E: Exception do
                begin
                  freeSWF();
                  Writeln(f, 'CMD_PAUSE error: ' + E.Message);
                  Flush(f);
                end;
              end;
            end;
            CMD_RESUME:
            begin
              try
                flaPreview.Play;
              except
                on E: Exception do
                begin
                  freeSWF();
                  Writeln(f, 'CMD_RESUME error: ' + E.Message);
                  Flush(f);
                end;
              end;
            end;
            CMD_PLAYING:
            begin
              buffer[0] := 0;
              try
                if flaPreview.ReadyState = 4 then
                  if flaPreview.IsPlaying then
                    buffer[0] := 1;
              except
                on E: Exception do
                begin
                  freeSWF();
                  Writeln(f, 'CMD_PLAYING error: ' + E.Message);
                  Flush(f);
                end;
              end;

              WritePipe(pipe, buffer, 1);
            end;
            CMD_REWIND:
            begin
              try
                flaPreview.Rewind;
              except
                on E: Exception do
                begin
                  freeSWF();
                  Writeln(f, 'CMD_REWIND error: ' + E.Message);
                  Flush(f);
                end;
              end;
            end;
            CMD_GOTO:
            begin
              ReadPipe(pipe, buffer, 2);
              val := (buffer[0] shl 8) + buffer[1];
              try
                flaPreview.GotoFrame(val);
              except
                on E: Exception do
                begin
                  freeSWF();
                  Writeln(f, 'CMD_GOTO error: ' + E.Message);
                  Flush(f);
                end;
              end;
            end;
            CMD_CALL:
            begin
              ReadPipe(pipe, buffer, 2);
              val := (buffer[0] shl 8) + buffer[1];
              ReadPipe(pipe, buffer, val);
              SetString(vals, PChar(Addr(buffer)), val);
              vals := '';
              try
                vals := flaPreview.CallFunction(vals);
              except
                on E: Exception do
                begin
                  freeSWF();
                  Writeln(f, 'CMD_CALL error: ' + E.Message);
                  Flush(f);
                end;
              end;
              val := length(vals);
              buffer[0] := (val shr 8) mod 256;
              buffer[1] := val mod 256;
              WritePipe(pipe, buffer, 2);
              Move(vals[1], buffer, val);
              WritePipe(pipe, buffer, val);
            end;
            CMD_GETVARIABLE:
            begin
              ReadPipe(pipe, buffer, 2);
              val := (buffer[0] shl 8) + buffer[1];
              ReadPipe(pipe, buffer, val);
              SetString(vals, PChar(Addr(buffer)), val);
              vals := '';
              try
                vals := flaPreview.GetVariable(vals);
              except
                on E: Exception do
                begin
                  freeSWF();
                  Writeln(f, 'CMD_GETVARIABLE error: ' + E.Message);
                  Flush(f);
                end;
              end;
              val := length(vals);
              buffer[0] := (val shr 8) mod 256;
              buffer[1] := val mod 256;
              WritePipe(pipe, buffer, 2);
              Move(vals[1], buffer, val);
              WritePipe(pipe, buffer, val);
            end;
            CMD_SETVARIABLE:
            begin
              ReadPipe(pipe, buffer, 2);
              val := (buffer[0] shl 8) + buffer[1];
              ReadPipe(pipe, buffer, val);
              SetString(vars, PChar(Addr(buffer)), val);

              ReadPipe(pipe, buffer, 2);
              val := (buffer[0] shl 8) + buffer[1];
              ReadPipe(pipe, buffer, val);
              SetString(vals, PChar(Addr(buffer)), val);

              try
                flaPreview.SetVariable(vars, vals);
              except
                on E: Exception do
                begin
                  freeSWF();
                  Writeln(f, 'CMD_SETVARIABLE error: ' + E.Message);
                  Flush(f);
                end;
              end;
            end;
          end;
        except
          on E: Exception do
          begin
            freeSWF();
            Writeln(f, 'FATAL ERROR: ' + E.Message + ' cmd: ' + IntToStr(cmd));
            Flush(f);
          end;
        end;
      until False;

      CloseHandle(pipe);
    end;
  except
    on E: Exception do
    begin
    end;
  end;
end;

procedure TfrmMain.FormActivate(Sender: TObject);
begin

  if (ParamCount >= 2) then
  begin
    flaPreview.Parent := frmMain;

    ShowWindow(Application.Handle, SW_HIDE);
    SetWindowLong(Application.Handle, GWL_EXSTYLE,
      getWindowLong(Application.Handle, GWL_EXSTYLE) or WS_EX_TOOLWINDOW);
    ShowWindow(Application.Handle, SW_SHOW);

    SetForegroundWindow(HWND(StrToInt(ParamStr(2))));
    frmMain.Caption := 'FlashPlayerWindow_' + ParamStr(2);
    Application.Title := 'FlashPlayerWindow_' + ParamStr(2);
    target := HWND(StrToInt(ParamStr(1)));

    SetWindowLong(frmMain.Handle, GWL_STYLE, 0);
    ShowWindow(frmMain.Handle, SW_SHOW);

    frmMain.Left := 0;
    frmMain.Top  := 0;
    Windows.SetParent(frmMain.Handle, target);

    t := TPipeThread.Create(True);
    t.Resume;
  end;
end;

procedure TfrmMain.FormCreate(Sender: TObject);
begin
  if (ParamCount >= 2) then
  begin
    flaPreview := TMySWF.Create(frmMain);
    flaPreview.AllowScriptAccess := 'always';
    flaPreview.BackgroundColor := -1;
  end;
end;

procedure TfrmMain.FormDestroy(Sender: TObject);
begin
  t.Free;
end;

procedure TfrmMain.tmrWatchDogTimer(Sender: TObject);
begin
  if target <> 0 then
  begin
    if not IsWindow(target) then
    begin
      Application.Terminate;
    end;
  end;
end;

end.
