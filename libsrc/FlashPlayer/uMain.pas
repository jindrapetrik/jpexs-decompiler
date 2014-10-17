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
      procedure NoRightClick(var Msg:TMsg; var handled:Boolean);
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
    bgColor:   integer;
    bgTColor: TColor;
    zoom:     integer;
    scalemode: integer;
    quality: integer;
    procedure Execute; override;
    procedure displaySWF;
    procedure freeSWF;
    procedure setPos;
    procedure setBGColor;
    procedure setZoom;
    procedure setQuality;
    procedure setScalemode;
  end;

  TBuf = array[0..255] of byte;

var
  frmMain:    TfrmMain;
  k:          string;
  t:          TPipeThread;
  flaPreview: TMySWF;
  target:     HWND = 0;
  clicked: integer = 0;
  xpos : integer =  0;
  ypos : integer = 0;

implementation

{$R *.dfm}



procedure TfrmMain.NoRightClick(var Msg:TMsg; var handled:Boolean);
begin
  if((Msg.message = WM_LBUTTONDOWN) and (Msg.wParam  = MK_LBUTTON)) then
   begin
     clicked := 1;
     xpos := LOWORD(Msg.lParam);
     ypos := HIWORD(Msg.lParam);
   end;

  if((Msg.message = WM_RBUTTONDOWN) and (Msg.wParam  = MK_RBUTTON)) then
   begin
     clicked := 2;
     xpos := LOWORD(Msg.lParam);
     ypos := HIWORD(Msg.lParam);
     handled := true;
   end
  else
    handled := false;
end;


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
  flaPreview.BackgroundColor := self.bgColor;
  frmMain.Color := self.bgTColor;
end;


procedure TPipeThread.setPos();
begin
  SetWindowPos(frmMain.Handle, 0, 0, 0, self.w, self.h, SWP_SHOWWINDOW);
  flaPreview.Left := 0;
  flaPreview.Top  := 0;
  flaPreview.Width := self.w;
  flaPreview.Height := self.h;
  flaPreview.CreateWnd;
end;



procedure TPipeThread.setZoom();
begin
  flaPreview.Zoom(self.zoom);
end;

procedure TPipeThread.setQuality();
begin
  flaPreview.Quality := self.quality;
end;

procedure TPipeThread.setScalemode();
begin
  flaPreview.ScaleMode := self.scalemode;
end;

procedure TPipeThread.Execute();
var
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
  CMD_CHECKCLICK = 14;
  CMD_ZOOM = 15;
  CMD_SET_QUALITY = 16;
  CMD_SET_SCALEMODE = 17;
begin

  try
    pipename := PAnsiChar('\\.\\pipe\ffdec_flashplayer_' + ParamStr(1));
    begin
      pipe := CreateFile(pipename, GENERIC_READ or GENERIC_WRITE,
        FILE_SHARE_READ or FILE_SHARE_WRITE, nil, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, 0);

      repeat
        try
          ReadPipe(pipe, buffer, 1);
          cmd := buffer[0];
          case cmd of
            CMD_SET_QUALITY:
            begin
              ReadPipe(pipe, buffer, 1);
              self.quality := buffer[0];
              Synchronize(setQuality);
            end;
            CMD_SET_SCALEMODE:
            begin
              ReadPipe(pipe, buffer, 1);
              self.scalemode := buffer[0];
              Synchronize(setScalemode);
            end;
            CMD_CHECKCLICK:
            begin
              buffer[0]:=clicked;
              len := 1;
              if clicked>0 then
              begin
                len := len + 4;
                buffer[1] := (xpos shr 8) mod 256;
                buffer[2] := xpos mod 256;
                buffer[3] := (ypos shr 8) mod 256;
                buffer[4] := ypos mod 256;
              end;
              clicked := 0;
              xpos := 0;
              ypos := 0;

              WritePipe(pipe,buffer,len);
            end;
            CMD_ZOOM:
            begin
              ReadPipe(pipe, buffer, 2);
              self.zoom := buffer[0] * 256 + buffer[1];
              Synchronize(setZoom);
            end;
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
              self.bgColor := (buffer[0] shl 16)+(buffer[1] shl 8)+(buffer[2]);
              self.bgTColor := RGB(buffer[0],buffer[1],buffer[2]);
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
                end;
              end;
            end;
            CMD_CALL:
            begin
              ReadPipe(pipe, buffer, 2);
              val := (buffer[0] shl 8) + buffer[1];
              ReadPipe(pipe, buffer, val);
              SetString(vals, PChar(Addr(buffer)), val);
              try
                vals := flaPreview.CallFunction(vals);
              except
                on E: Exception do
                begin
                  vals := '';
                  freeSWF();
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
              try
                vals := flaPreview.GetVariable(vals);
              except
                on E: Exception do
                begin
                  vals := '';
                  freeSWF();
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
                end;
              end;
            end;
          end;
        except
          on E: Exception do
          begin
            freeSWF();
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
    Application.OnMessage := NoRightClick;
    try
      flaPreview := TMySWF.Create(frmMain);
      flaPreview.Scale := 'noscale';
      flaPreview.WMode := 'direct';
      flaPreview.Menu := false;
      flaPreview.AllowScriptAccess := 'always';
      flaPreview.BackgroundColor := -1;
    except
      Application.Terminate;
    end;
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
