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
    Procedure CreateWnd; override;
  end;

  type
  TPipeThread = class(TThread)
  protected
    flashFile:string;
    w:integer;
    h:integer;
    bgColor:TColor;
    procedure Execute; override;
    procedure displaySWF;
    procedure setPos;
    procedure setBGColor;
  end;

  TBuf = array[0..255] of byte;
var
  frmMain: TfrmMain;
  k:String;
  t:TPipeThread;
  flaPreview: TMySWF;
  target:HWND=0;
implementation

{$R *.dfm}



Procedure TMySWF.CreateWnd;
begin
  inherited;
end; 

function arrToStr(k:TBuf;len:integer):string ;
var s:string;
i:integer;
begin
 s:='';
 for i:=0 to len-1 do
  begin
    if k[i]=0 then
     break;
    s:=s +''+ chr(k[i]);
  end;
  Result:=s;
end;

procedure TPipeThread.displaySWF();
begin
  windows.SetParent(frmMain.Handle,target);
  flaPreview.Stop;
  flaPreview.Movie := '';
  flaPreview.Free;
  flaPreview := nil;
  flaPreview := TMySWF.Create(frmMain);
  flaPreview.Left:=0;
  flaPreview.Top:=0;
  flaPreview.Width:=self.w;
  flaPreview.Height:=self.h;
  frmMain.Caption:='set movie:'+flashFile;
  flaPreview.Parent := frmMain;
  flaPreview.Movie:=flashFile;

end;

procedure TPipeThread.setBGColor();
begin
  frmMain.Color := self.bgColor;
end;


procedure TPipeThread.setPos();
begin
SetWindowPos(frmMain.Handle,0,0,0,self.w,self.h,SWP_SHOWWINDOW);
flaPreview.Left:=0;
flaPreview.Top:=0;
flaPreview.Width:=self.w;
flaPreview.Height:=self.h;
flaPreview.CreateWnd;
//displaySWF();
end;

procedure TPipeThread.Execute();
var pipe:cardinal;
numBytesRead:DWORD;
buffer:TBuf;
pipename:PAnsiChar;
len:integer;
cmd:integer;
written:cardinal;
val:cardinal;
vals:String;
vars:String;

const
 CMD_PLAY = 1;
 CMD_RESIZE = 2;
 CMD_BGCOLOR = 3;
 CMD_CURRENT_FRAME = 4;
 CMD_TOTAL_FRAMES = 5;
 CMD_PAUSE = 6;
 CMD_RESUME = 7;
 CMD_PLAYING = 8;
 CMD_REWIND = 9;
 CMD_GOTO = 10;
 CMD_CALL = 11;
 CMD_GETVARIABLE = 12;
 CMD_SETVARIABLE = 13;
begin

pipename:=PAnsiChar('\\.\\pipe\ffdec_flashplayer_'+ParamStr(1));
while (not self.Terminated) do
begin
  pipe:=CreateFile(pipename,GENERIC_READ or GENERIC_WRITE,
		FILE_SHARE_READ or FILE_SHARE_WRITE,
		nil,
		OPEN_EXISTING,
		FILE_ATTRIBUTE_NORMAL,
		0
	);
	numBytesRead:= 0;

  repeat
  begin
        ReadFile(pipe,buffer,1,numBytesRead,nil);
        cmd:=buffer[0];
        case cmd of
        CMD_PLAY:
        begin
          ReadFile(pipe,buffer,1,numBytesRead,nil);
          if(numBytesRead>0) then
          begin
            len:=buffer[0];
            ReadFile(pipe,buffer,len,numBytesRead,nil);
            self.flashFile:=arrToStr(buffer,numBytesRead);
            Synchronize(displaySWF);
          end;
        end;
        CMD_RESIZE:
        begin
          ReadFile(pipe,buffer,4,numBytesRead,nil);
          self.w:=buffer[0]*256+buffer[1];
          self.h:=buffer[2]*256+buffer[3];
          Synchronize(setPos);
        end;
        CMD_BGCOLOR:
        begin
          ReadFile(pipe,buffer,3,numBytesRead,nil);
          self.bgColor := RGB(buffer[0],buffer[1],buffer[2]);
          Synchronize(setBGColor);
        end;
        CMD_CURRENT_FRAME:
        begin
          if flaPreview.ReadyState = 4 then
            val:=flaPreview.CurrentFrame
            else
            val:=0;
          buffer[0]:=(val shr 8) mod 256;
          buffer[1]:=val mod 256;
          WriteFile(pipe,buffer,2,written,nil);
        end;
        CMD_TOTAL_FRAMES:
        begin
          if flaPreview.ReadyState = 4 then
            val:=flaPreview.TotalFrames
            else
            val:=0;
          buffer[0]:=(val shr 8) mod 256;
          buffer[1]:=val mod 256;
          WriteFile(pipe,buffer,2,written,nil);
        end;
        CMD_PAUSE:
        begin
          flaPreview.Stop;
        end;
        CMD_RESUME:
        begin
          flaPreview.Play;
        end;
        CMD_PLAYING:
        begin
          buffer[0]:=0;
          if flaPreview.ReadyState = 4 then
            if flaPreview.IsPlaying then
              buffer[0]:=1;


          WriteFile(pipe,buffer,1,written,nil);
        end;
        CMD_REWIND:
        begin
          flaPreview.Rewind;
        end;
        CMD_GOTO:
        begin
          ReadFile(pipe,buffer,2,numBytesRead,nil);
          val := (buffer[0] shl 8) + buffer[1];
          flaPreview.GotoFrame(val);
        end;
        CMD_CALL:
        begin
          ReadFile(pipe,buffer,2,numBytesRead,nil);
          val := (buffer[0] shl 8) + buffer[1];
          ReadFile(pipe,buffer,val,numBytesRead,nil);
          SetString(vals, PChar(Addr(buffer)), val);
          vals:=flaPreview.CallFunction(vals);
          val:=length(vals);
          buffer[0]:=(val shr 8) mod 256;
          buffer[1]:=val mod 256;
          WriteFile(pipe,buffer,2,written,nil);
          Move(vals[1], buffer, val);
          WriteFile(pipe,buffer,val,written,nil);
        end;
        CMD_GETVARIABLE:
        begin
          ReadFile(pipe,buffer,2,numBytesRead,nil);
          val := (buffer[0] shl 8) + buffer[1];
          ReadFile(pipe,buffer,val,numBytesRead,nil);
          SetString(vals, PChar(Addr(buffer)), val);
          vals:=flaPreview.GetVariable(vals);
          val:=length(vals);
          buffer[0]:=(val shr 8) mod 256;
          buffer[1]:=val mod 256;
          WriteFile(pipe,buffer,2,written,nil);
          Move(vals[1], buffer, val);
          WriteFile(pipe,buffer,val,written,nil);
        end;
        CMD_SETVARIABLE:
        begin
          ReadFile(pipe,buffer,2,numBytesRead,nil);
          val := (buffer[0] shl 8) + buffer[1];
          ReadFile(pipe,buffer,val,numBytesRead,nil);
          SetString(vars, PChar(Addr(buffer)), val);

          ReadFile(pipe,buffer,2,numBytesRead,nil);
          val := (buffer[0] shl 8) + buffer[1];
          ReadFile(pipe,buffer,val,numBytesRead,nil);
          SetString(vals, PChar(Addr(buffer)), val);

          flaPreview.SetVariable(vars,vals);

        end;
        end;
  end
  until numBytesRead<=0;
  CloseHandle(pipe);


end;

end;

procedure TfrmMain.FormActivate(Sender: TObject);
begin




if(ParamCount>=2) then
  begin
   flaPreview.Parent := frmMain;

    ShowWindow(Application.Handle, SW_HIDE) ;
    SetWindowLong(Application.Handle, GWL_EXSTYLE, getWindowLong(Application.Handle, GWL_EXSTYLE) or WS_EX_TOOLWINDOW) ;
    ShowWindow(Application.Handle, SW_SHOW);

    SetForegroundWindow(HWND(strtoint(ParamStr(2))));
    frmMain.Caption:='FlashPlayerWindow_'+ParamStr(2);
    Application.Title:='FlashPlayerWindow_'+ParamStr(2);
    target:=HWND(strtoint(ParamStr(1)));

    SetWindowLong(frmMain.Handle, GWL_STYLE, 0);
    ShowWindow(frmMain.Handle, SW_SHOW);

    frmMain.Left:=0;
    frmMain.Top:=0;
    windows.SetParent(frmMain.Handle,target);

    t:=TPipeThread.Create(true);
    t.Resume;
  end;
end;

procedure TfrmMain.FormCreate(Sender: TObject);
begin
 if(ParamCount>=2) then
 begin
   flaPreview := TMySWF.Create(frmMain);
   flaPreview.AllowScriptAccess := 'always';
   flaPreview.BackgroundColor:=-1;
  end;
end;

procedure TfrmMain.FormDestroy(Sender: TObject);
begin
t.Free;
end;

procedure TfrmMain.tmrWatchDogTimer(Sender: TObject);
begin
 if target<>0 then
 begin
    if not IsWindow(target) then
    begin
      Application.Terminate;
    end;
 end;
end;

end.
