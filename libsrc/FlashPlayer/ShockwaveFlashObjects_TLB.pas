unit ShockwaveFlashObjects_TLB;

// ************************************************************************ //
// WARNING                                                                    
// -------                                                                    
// The types declared in this file were generated from data read from a       
// Type Library. If this type library is explicitly or indirectly (via        
// another type library referring to this type library) re-imported, or the   
// 'Refresh' command of the Type Library Editor activated while editing the   
// Type Library, the contents of this file will be regenerated and all        
// manual modifications will be lost.                                         
// ************************************************************************ //

// PASTLWTR : 1.2
// File generated on 28. 9. 2014 14:31:49 from Type Library described below.

// ************************************************************************  //
// Type Lib: C:\Windows\System32\Macromed\Flash\Flash.ocx (1)
// LIBID: {D27CDB6B-AE6D-11CF-96B8-444553540000}
// LCID: 0
// Helpfile: 
// HelpString: Shockwave Flash
// DepndLst: 
//   (1) v2.0 stdole, (C:\Windows\SysWOW64\stdole2.tlb)
// Errors:
//   Hint: Parameter 'label' of IShockwaveFlash.TGotoLabel changed to 'label_'
//   Hint: Parameter 'property' of IShockwaveFlash.TSetProperty changed to 'property_'
//   Hint: Parameter 'property' of IShockwaveFlash.TGetProperty changed to 'property_'
//   Hint: Parameter 'label' of IShockwaveFlash.TCallLabel changed to 'label_'
//   Hint: Parameter 'property' of IShockwaveFlash.TSetPropertyNum changed to 'property_'
//   Hint: Parameter 'property' of IShockwaveFlash.TGetPropertyNum changed to 'property_'
//   Hint: Parameter 'property' of IShockwaveFlash.TGetPropertyAsNumber changed to 'property_'
// ************************************************************************ //
{$TYPEDADDRESS OFF} // Unit must be compiled without type-checked pointers. 
{$WARN SYMBOL_PLATFORM OFF}
{$WRITEABLECONST ON}
{$VARPROPSETTER ON}
interface

uses Windows, ActiveX, Classes, Graphics, OleCtrls, OleServer, StdVCL, Variants;
  


// *********************************************************************//
// GUIDS declared in the TypeLibrary. Following prefixes are used:        
//   Type Libraries     : LIBID_xxxx                                      
//   CoClasses          : CLASS_xxxx                                      
//   DISPInterfaces     : DIID_xxxx                                       
//   Non-DISP interfaces: IID_xxxx                                        
// *********************************************************************//
const
  // TypeLibrary Major and minor versions
  ShockwaveFlashObjectsMajorVersion = 1;
  ShockwaveFlashObjectsMinorVersion = 0;

  LIBID_ShockwaveFlashObjects: TGUID = '{D27CDB6B-AE6D-11CF-96B8-444553540000}';

  IID_IShockwaveFlash: TGUID = '{D27CDB6C-AE6D-11CF-96B8-444553540000}';
  IID_ICanHandleException: TGUID = '{C5598E60-B307-11D1-B27D-006008C3FBFB}';
  DIID__IShockwaveFlashEvents: TGUID = '{D27CDB6D-AE6D-11CF-96B8-444553540000}';
  CLASS_ShockwaveFlash: TGUID = '{D27CDB6E-AE6D-11CF-96B8-444553540000}';
  IID_IFlashFactory: TGUID = '{D27CDB70-AE6D-11CF-96B8-444553540000}';
  IID_IDispatchEx: TGUID = '{A6EF9860-C720-11D0-9337-00A0C90DCAA9}';
  IID_IFlashObjectInterface: TGUID = '{D27CDB72-AE6D-11CF-96B8-444553540000}';
  IID_IServiceProvider: TGUID = '{6D5140C1-7436-11CE-8034-00AA006009FA}';
  CLASS_FlashObjectInterface: TGUID = '{D27CDB71-AE6D-11CF-96B8-444553540000}';
  IID_IFlashObject: TGUID = '{86230738-D762-4C50-A2DE-A753E5B1686F}';
  CLASS_FlashObject: TGUID = '{E0920E11-6B65-4D5D-9C58-B1FC5C07DC43}';
type

// *********************************************************************//
// Forward declaration of types defined in TypeLibrary                    
// *********************************************************************//
  IShockwaveFlash = interface;
  IShockwaveFlashDisp = dispinterface;
  ICanHandleException = interface;
  _IShockwaveFlashEvents = dispinterface;
  IFlashFactory = interface;
  IDispatchEx = interface;
  IFlashObjectInterface = interface;
  IServiceProvider = interface;
  IFlashObject = interface;
  IFlashObjectDisp = dispinterface;

// *********************************************************************//
// Declaration of CoClasses defined in Type Library                       
// (NOTE: Here we map each CoClass to its Default Interface)              
// *********************************************************************//
  ShockwaveFlash = IShockwaveFlash;
  FlashObjectInterface = IFlashObjectInterface;
  FlashObject = IFlashObject;


// *********************************************************************//
// Declaration of structures, unions and aliases.                         
// *********************************************************************//
  PUserType1 = ^EXCEPINFO; {*}
  POleVariant1 = ^OleVariant; {*}
  PUserType2 = ^DISPPARAMS; {*}
  PSYSUINT1 = ^SYSUINT; {*}
  PUserType3 = ^TGUID; {*}


// *********************************************************************//
// Interface: IShockwaveFlash
// Flags:     (4416) Dual OleAutomation Dispatchable
// GUID:      {D27CDB6C-AE6D-11CF-96B8-444553540000}
// *********************************************************************//
  IShockwaveFlash = interface(IDispatch)
    ['{D27CDB6C-AE6D-11CF-96B8-444553540000}']
    function Get_ReadyState: Integer; safecall;
    function Get_TotalFrames: Integer; safecall;
    function Get_Playing: WordBool; safecall;
    procedure Set_Playing(pVal: WordBool); safecall;
    function Get_Quality: SYSINT; safecall;
    procedure Set_Quality(pVal: SYSINT); safecall;
    function Get_ScaleMode: SYSINT; safecall;
    procedure Set_ScaleMode(pVal: SYSINT); safecall;
    function Get_AlignMode: SYSINT; safecall;
    procedure Set_AlignMode(pVal: SYSINT); safecall;
    function Get_BackgroundColor: Integer; safecall;
    procedure Set_BackgroundColor(pVal: Integer); safecall;
    function Get_Loop: WordBool; safecall;
    procedure Set_Loop(pVal: WordBool); safecall;
    function Get_Movie: WideString; safecall;
    procedure Set_Movie(const pVal: WideString); safecall;
    function Get_FrameNum: Integer; safecall;
    procedure Set_FrameNum(pVal: Integer); safecall;
    procedure SetZoomRect(left: Integer; top: Integer; right: Integer; bottom: Integer); safecall;
    procedure Zoom(factor: SYSINT); safecall;
    procedure Pan(x: Integer; y: Integer; mode: SYSINT); safecall;
    procedure Play; safecall;
    procedure Stop; safecall;
    procedure Back; safecall;
    procedure Forward; safecall;
    procedure Rewind; safecall;
    procedure StopPlay; safecall;
    procedure GotoFrame(FrameNum: Integer); safecall;
    function CurrentFrame: Integer; safecall;
    function IsPlaying: WordBool; safecall;
    function PercentLoaded: Integer; safecall;
    function FrameLoaded(FrameNum: Integer): WordBool; safecall;
    function FlashVersion: Integer; safecall;
    function Get_WMode: WideString; safecall;
    procedure Set_WMode(const pVal: WideString); safecall;
    function Get_SAlign: WideString; safecall;
    procedure Set_SAlign(const pVal: WideString); safecall;
    function Get_Menu: WordBool; safecall;
    procedure Set_Menu(pVal: WordBool); safecall;
    function Get_Base: WideString; safecall;
    procedure Set_Base(const pVal: WideString); safecall;
    function Get_Scale: WideString; safecall;
    procedure Set_Scale(const pVal: WideString); safecall;
    function Get_DeviceFont: WordBool; safecall;
    procedure Set_DeviceFont(pVal: WordBool); safecall;
    function Get_EmbedMovie: WordBool; safecall;
    procedure Set_EmbedMovie(pVal: WordBool); safecall;
    function Get_BGColor: WideString; safecall;
    procedure Set_BGColor(const pVal: WideString); safecall;
    function Get_Quality2: WideString; safecall;
    procedure Set_Quality2(const pVal: WideString); safecall;
    procedure LoadMovie(layer: SYSINT; const url: WideString); safecall;
    procedure TGotoFrame(const target: WideString; FrameNum: Integer); safecall;
    procedure TGotoLabel(const target: WideString; const label_: WideString); safecall;
    function TCurrentFrame(const target: WideString): Integer; safecall;
    function TCurrentLabel(const target: WideString): WideString; safecall;
    procedure TPlay(const target: WideString); safecall;
    procedure TStopPlay(const target: WideString); safecall;
    procedure SetVariable(const name: WideString; const value: WideString); safecall;
    function GetVariable(const name: WideString): WideString; safecall;
    procedure TSetProperty(const target: WideString; property_: SYSINT; const value: WideString); safecall;
    function TGetProperty(const target: WideString; property_: SYSINT): WideString; safecall;
    procedure TCallFrame(const target: WideString; FrameNum: SYSINT); safecall;
    procedure TCallLabel(const target: WideString; const label_: WideString); safecall;
    procedure TSetPropertyNum(const target: WideString; property_: SYSINT; value: Double); safecall;
    function TGetPropertyNum(const target: WideString; property_: SYSINT): Double; safecall;
    function TGetPropertyAsNumber(const target: WideString; property_: SYSINT): Double; safecall;
    function Get_SWRemote: WideString; safecall;
    procedure Set_SWRemote(const pVal: WideString); safecall;
    function Get_FlashVars: WideString; safecall;
    procedure Set_FlashVars(const pVal: WideString); safecall;
    function Get_AllowScriptAccess: WideString; safecall;
    procedure Set_AllowScriptAccess(const pVal: WideString); safecall;
    function Get_MovieData: WideString; safecall;
    procedure Set_MovieData(const pVal: WideString); safecall;
    function Get_InlineData: IUnknown; safecall;
    procedure Set_InlineData(const ppIUnknown: IUnknown); safecall;
    function Get_SeamlessTabbing: WordBool; safecall;
    procedure Set_SeamlessTabbing(pVal: WordBool); safecall;
    procedure EnforceLocalSecurity; safecall;
    function Get_Profile: WordBool; safecall;
    procedure Set_Profile(pVal: WordBool); safecall;
    function Get_ProfileAddress: WideString; safecall;
    procedure Set_ProfileAddress(const pVal: WideString); safecall;
    function Get_ProfilePort: Integer; safecall;
    procedure Set_ProfilePort(pVal: Integer); safecall;
    function CallFunction(const request: WideString): WideString; safecall;
    procedure SetReturnValue(const returnValue: WideString); safecall;
    procedure DisableLocalSecurity; safecall;
    function Get_AllowNetworking: WideString; safecall;
    procedure Set_AllowNetworking(const pVal: WideString); safecall;
    function Get_AllowFullScreen: WideString; safecall;
    procedure Set_AllowFullScreen(const pVal: WideString); safecall;
    function Get_AllowFullScreenInteractive: WideString; safecall;
    procedure Set_AllowFullScreenInteractive(const pVal: WideString); safecall;
    function Get_IsDependent: WordBool; safecall;
    procedure Set_IsDependent(pVal: WordBool); safecall;
    property ReadyState: Integer read Get_ReadyState;
    property TotalFrames: Integer read Get_TotalFrames;
    property Playing: WordBool read Get_Playing write Set_Playing;
    property Quality: SYSINT read Get_Quality write Set_Quality;
    property ScaleMode: SYSINT read Get_ScaleMode write Set_ScaleMode;
    property AlignMode: SYSINT read Get_AlignMode write Set_AlignMode;
    property BackgroundColor: Integer read Get_BackgroundColor write Set_BackgroundColor;
    property Loop: WordBool read Get_Loop write Set_Loop;
    property Movie: WideString read Get_Movie write Set_Movie;
    property FrameNum: Integer read Get_FrameNum write Set_FrameNum;
    property WMode: WideString read Get_WMode write Set_WMode;
    property SAlign: WideString read Get_SAlign write Set_SAlign;
    property Menu: WordBool read Get_Menu write Set_Menu;
    property Base: WideString read Get_Base write Set_Base;
    property Scale: WideString read Get_Scale write Set_Scale;
    property DeviceFont: WordBool read Get_DeviceFont write Set_DeviceFont;
    property EmbedMovie: WordBool read Get_EmbedMovie write Set_EmbedMovie;
    property BGColor: WideString read Get_BGColor write Set_BGColor;
    property Quality2: WideString read Get_Quality2 write Set_Quality2;
    property SWRemote: WideString read Get_SWRemote write Set_SWRemote;
    property FlashVars: WideString read Get_FlashVars write Set_FlashVars;
    property AllowScriptAccess: WideString read Get_AllowScriptAccess write Set_AllowScriptAccess;
    property MovieData: WideString read Get_MovieData write Set_MovieData;
    property InlineData: IUnknown read Get_InlineData write Set_InlineData;
    property SeamlessTabbing: WordBool read Get_SeamlessTabbing write Set_SeamlessTabbing;
    property Profile: WordBool read Get_Profile write Set_Profile;
    property ProfileAddress: WideString read Get_ProfileAddress write Set_ProfileAddress;
    property ProfilePort: Integer read Get_ProfilePort write Set_ProfilePort;
    property AllowNetworking: WideString read Get_AllowNetworking write Set_AllowNetworking;
    property AllowFullScreen: WideString read Get_AllowFullScreen write Set_AllowFullScreen;
    property AllowFullScreenInteractive: WideString read Get_AllowFullScreenInteractive write Set_AllowFullScreenInteractive;
    property IsDependent: WordBool read Get_IsDependent write Set_IsDependent;
  end;

// *********************************************************************//
// DispIntf:  IShockwaveFlashDisp
// Flags:     (4416) Dual OleAutomation Dispatchable
// GUID:      {D27CDB6C-AE6D-11CF-96B8-444553540000}
// *********************************************************************//
  IShockwaveFlashDisp = dispinterface
    ['{D27CDB6C-AE6D-11CF-96B8-444553540000}']
    property ReadyState: Integer readonly dispid -525;
    property TotalFrames: Integer readonly dispid 124;
    property Playing: WordBool dispid 125;
    property Quality: SYSINT dispid 105;
    property ScaleMode: SYSINT dispid 120;
    property AlignMode: SYSINT dispid 121;
    property BackgroundColor: Integer dispid 123;
    property Loop: WordBool dispid 106;
    property Movie: WideString dispid 102;
    property FrameNum: Integer dispid 107;
    procedure SetZoomRect(left: Integer; top: Integer; right: Integer; bottom: Integer); dispid 109;
    procedure Zoom(factor: SYSINT); dispid 118;
    procedure Pan(x: Integer; y: Integer; mode: SYSINT); dispid 119;
    procedure Play; dispid 112;
    procedure Stop; dispid 113;
    procedure Back; dispid 114;
    procedure Forward; dispid 115;
    procedure Rewind; dispid 116;
    procedure StopPlay; dispid 126;
    procedure GotoFrame(FrameNum: Integer); dispid 127;
    function CurrentFrame: Integer; dispid 128;
    function IsPlaying: WordBool; dispid 129;
    function PercentLoaded: Integer; dispid 130;
    function FrameLoaded(FrameNum: Integer): WordBool; dispid 131;
    function FlashVersion: Integer; dispid 132;
    property WMode: WideString dispid 133;
    property SAlign: WideString dispid 134;
    property Menu: WordBool dispid 135;
    property Base: WideString dispid 136;
    property Scale: WideString dispid 137;
    property DeviceFont: WordBool dispid 138;
    property EmbedMovie: WordBool dispid 139;
    property BGColor: WideString dispid 140;
    property Quality2: WideString dispid 141;
    procedure LoadMovie(layer: SYSINT; const url: WideString); dispid 142;
    procedure TGotoFrame(const target: WideString; FrameNum: Integer); dispid 143;
    procedure TGotoLabel(const target: WideString; const label_: WideString); dispid 144;
    function TCurrentFrame(const target: WideString): Integer; dispid 145;
    function TCurrentLabel(const target: WideString): WideString; dispid 146;
    procedure TPlay(const target: WideString); dispid 147;
    procedure TStopPlay(const target: WideString); dispid 148;
    procedure SetVariable(const name: WideString; const value: WideString); dispid 151;
    function GetVariable(const name: WideString): WideString; dispid 152;
    procedure TSetProperty(const target: WideString; property_: SYSINT; const value: WideString); dispid 153;
    function TGetProperty(const target: WideString; property_: SYSINT): WideString; dispid 154;
    procedure TCallFrame(const target: WideString; FrameNum: SYSINT); dispid 155;
    procedure TCallLabel(const target: WideString; const label_: WideString); dispid 156;
    procedure TSetPropertyNum(const target: WideString; property_: SYSINT; value: Double); dispid 157;
    function TGetPropertyNum(const target: WideString; property_: SYSINT): Double; dispid 158;
    function TGetPropertyAsNumber(const target: WideString; property_: SYSINT): Double; dispid 172;
    property SWRemote: WideString dispid 159;
    property FlashVars: WideString dispid 170;
    property AllowScriptAccess: WideString dispid 171;
    property MovieData: WideString dispid 190;
    property InlineData: IUnknown dispid 191;
    property SeamlessTabbing: WordBool dispid 192;
    procedure EnforceLocalSecurity; dispid 193;
    property Profile: WordBool dispid 194;
    property ProfileAddress: WideString dispid 195;
    property ProfilePort: Integer dispid 196;
    function CallFunction(const request: WideString): WideString; dispid 198;
    procedure SetReturnValue(const returnValue: WideString); dispid 199;
    procedure DisableLocalSecurity; dispid 200;
    property AllowNetworking: WideString dispid 201;
    property AllowFullScreen: WideString dispid 202;
    property AllowFullScreenInteractive: WideString dispid 501;
    property IsDependent: WordBool dispid 502;
  end;

// *********************************************************************//
// Interface: ICanHandleException
// Flags:     (0)
// GUID:      {C5598E60-B307-11D1-B27D-006008C3FBFB}
// *********************************************************************//
  ICanHandleException = interface(IUnknown)
    ['{C5598E60-B307-11D1-B27D-006008C3FBFB}']
    function CanHandleException(var pExcepInfo: EXCEPINFO; var pvar: OleVariant): HResult; stdcall;
  end;

// *********************************************************************//
// DispIntf:  _IShockwaveFlashEvents
// Flags:     (4112) Hidden Dispatchable
// GUID:      {D27CDB6D-AE6D-11CF-96B8-444553540000}
// *********************************************************************//
  _IShockwaveFlashEvents = dispinterface
    ['{D27CDB6D-AE6D-11CF-96B8-444553540000}']
    procedure OnReadyStateChange(newState: Integer); dispid -609;
    procedure OnProgress(percentDone: Integer); dispid 1958;
    procedure FSCommand(const command: WideString; const args: WideString); dispid 150;
    procedure FlashCall(const request: WideString); dispid 197;
  end;

// *********************************************************************//
// Interface: IFlashFactory
// Flags:     (0)
// GUID:      {D27CDB70-AE6D-11CF-96B8-444553540000}
// *********************************************************************//
  IFlashFactory = interface(IUnknown)
    ['{D27CDB70-AE6D-11CF-96B8-444553540000}']
  end;

// *********************************************************************//
// Interface: IDispatchEx
// Flags:     (4096) Dispatchable
// GUID:      {A6EF9860-C720-11D0-9337-00A0C90DCAA9}
// *********************************************************************//
  IDispatchEx = interface(IDispatch)
    ['{A6EF9860-C720-11D0-9337-00A0C90DCAA9}']
    function GetDispID(const bstrName: WideString; grfdex: LongWord; out pid: Integer): HResult; stdcall;
    function RemoteInvokeEx(id: Integer; lcid: LongWord; dwFlags: LongWord; var pdp: DISPPARAMS; 
                            out pvarRes: OleVariant; out pei: EXCEPINFO; 
                            const pspCaller: IServiceProvider; cvarRefArg: SYSUINT; 
                            var rgiRefArg: SYSUINT; var rgvarRefArg: OleVariant): HResult; stdcall;
    function DeleteMemberByName(const bstrName: WideString; grfdex: LongWord): HResult; stdcall;
    function DeleteMemberByDispID(id: Integer): HResult; stdcall;
    function GetMemberProperties(id: Integer; grfdexFetch: LongWord; out pgrfdex: LongWord): HResult; stdcall;
    function GetMemberName(id: Integer; out pbstrName: WideString): HResult; stdcall;
    function GetNextDispID(grfdex: LongWord; id: Integer; out pid: Integer): HResult; stdcall;
    function GetNameSpaceParent(out ppunk: IUnknown): HResult; stdcall;
  end;

// *********************************************************************//
// Interface: IFlashObjectInterface
// Flags:     (4096) Dispatchable
// GUID:      {D27CDB72-AE6D-11CF-96B8-444553540000}
// *********************************************************************//
  IFlashObjectInterface = interface(IDispatchEx)
    ['{D27CDB72-AE6D-11CF-96B8-444553540000}']
  end;

// *********************************************************************//
// Interface: IServiceProvider
// Flags:     (0)
// GUID:      {6D5140C1-7436-11CE-8034-00AA006009FA}
// *********************************************************************//
  IServiceProvider = interface(IUnknown)
    ['{6D5140C1-7436-11CE-8034-00AA006009FA}']
    function RemoteQueryService(var guidService: TGUID; var riid: TGUID; out ppvObject: IUnknown): HResult; stdcall;
  end;

// *********************************************************************//
// Interface: IFlashObject
// Flags:     (4544) Dual NonExtensible OleAutomation Dispatchable
// GUID:      {86230738-D762-4C50-A2DE-A753E5B1686F}
// *********************************************************************//
  IFlashObject = interface(IDispatchEx)
    ['{86230738-D762-4C50-A2DE-A753E5B1686F}']
  end;

// *********************************************************************//
// DispIntf:  IFlashObjectDisp
// Flags:     (4544) Dual NonExtensible OleAutomation Dispatchable
// GUID:      {86230738-D762-4C50-A2DE-A753E5B1686F}
// *********************************************************************//
  IFlashObjectDisp = dispinterface
    ['{86230738-D762-4C50-A2DE-A753E5B1686F}']
    procedure GetDispID(const bstrName: WideString; grfdex: LongWord; out pid: Integer); dispid 1610743808;
    procedure RemoteInvokeEx(id: Integer; lcid: LongWord; dwFlags: LongWord; 
                             var pdp: {??DISPPARAMS}OleVariant; out pvarRes: OleVariant; 
                             out pei: {??EXCEPINFO}OleVariant; const pspCaller: IServiceProvider; 
                             cvarRefArg: SYSUINT; var rgiRefArg: SYSUINT; 
                             var rgvarRefArg: OleVariant); dispid 1610743809;
    procedure DeleteMemberByName(const bstrName: WideString; grfdex: LongWord); dispid 1610743810;
    procedure DeleteMemberByDispID(id: Integer); dispid 1610743811;
    procedure GetMemberProperties(id: Integer; grfdexFetch: LongWord; out pgrfdex: LongWord); dispid 1610743812;
    procedure GetMemberName(id: Integer; out pbstrName: WideString); dispid 1610743813;
    procedure GetNextDispID(grfdex: LongWord; id: Integer; out pid: Integer); dispid 1610743814;
    procedure GetNameSpaceParent(out ppunk: IUnknown); dispid 1610743815;
  end;


// *********************************************************************//
// OLE Control Proxy class declaration
// Control Name     : TShockwaveFlash
// Help String      : Shockwave Flash
// Default Interface: IShockwaveFlash
// Def. Intf. DISP? : No
// Event   Interface: _IShockwaveFlashEvents
// TypeFlags        : (2) CanCreate
// *********************************************************************//
  TShockwaveFlashOnReadyStateChange = procedure(ASender: TObject; newState: Integer) of object;
  TShockwaveFlashOnProgress = procedure(ASender: TObject; percentDone: Integer) of object;
  TShockwaveFlashFSCommand = procedure(ASender: TObject; const command: WideString; 
                                                         const args: WideString) of object;
  TShockwaveFlashFlashCall = procedure(ASender: TObject; const request: WideString) of object;

  TShockwaveFlash = class(TOleControl)
  private
    FOnReadyStateChange: TShockwaveFlashOnReadyStateChange;
    FOnProgress: TShockwaveFlashOnProgress;
    FOnFSCommand: TShockwaveFlashFSCommand;
    FOnFlashCall: TShockwaveFlashFlashCall;
    FIntf: IShockwaveFlash;
    function  GetControlInterface: IShockwaveFlash;
  protected
    procedure CreateControl;
    procedure InitControlData; override;
    function Get_InlineData: IUnknown;
    procedure Set_InlineData(const ppIUnknown: IUnknown);
  public
    procedure SetZoomRect(left: Integer; top: Integer; right: Integer; bottom: Integer);
    procedure Zoom(factor: SYSINT);
    procedure Pan(x: Integer; y: Integer; mode: SYSINT);
    procedure Play;
    procedure Stop;
    procedure Back;
    procedure Forward;
    procedure Rewind;
    procedure StopPlay;
    procedure GotoFrame(FrameNum: Integer);
    function CurrentFrame: Integer;
    function IsPlaying: WordBool;
    function PercentLoaded: Integer;
    function FrameLoaded(FrameNum: Integer): WordBool;
    function FlashVersion: Integer;
    procedure LoadMovie(layer: SYSINT; const url: WideString);
    procedure TGotoFrame(const target: WideString; FrameNum: Integer);
    procedure TGotoLabel(const target: WideString; const label_: WideString);
    function TCurrentFrame(const target: WideString): Integer;
    function TCurrentLabel(const target: WideString): WideString;
    procedure TPlay(const target: WideString);
    procedure TStopPlay(const target: WideString);
    procedure SetVariable(const name: WideString; const value: WideString);
    function GetVariable(const name: WideString): WideString;
    procedure TSetProperty(const target: WideString; property_: SYSINT; const value: WideString);
    function TGetProperty(const target: WideString; property_: SYSINT): WideString;
    procedure TCallFrame(const target: WideString; FrameNum: SYSINT);
    procedure TCallLabel(const target: WideString; const label_: WideString);
    procedure TSetPropertyNum(const target: WideString; property_: SYSINT; value: Double);
    function TGetPropertyNum(const target: WideString; property_: SYSINT): Double;
    function TGetPropertyAsNumber(const target: WideString; property_: SYSINT): Double;
    procedure EnforceLocalSecurity;
    function CallFunction(const request: WideString): WideString;
    procedure SetReturnValue(const returnValue: WideString);
    procedure DisableLocalSecurity;
    property  ControlInterface: IShockwaveFlash read GetControlInterface;
    property  DefaultInterface: IShockwaveFlash read GetControlInterface;
    property ReadyState: Integer index -525 read GetIntegerProp;
    property TotalFrames: Integer index 124 read GetIntegerProp;
    property InlineData: IUnknown index 191 read GetIUnknownProp write SetIUnknownProp;
  published
    property Anchors;
    property  TabStop;
    property  Align;
    property  DragCursor;
    property  DragMode;
    property  ParentShowHint;
    property  PopupMenu;
    property  ShowHint;
    property  TabOrder;
    property  Visible;
    property  OnDragDrop;
    property  OnDragOver;
    property  OnEndDrag;
    property  OnEnter;
    property  OnExit;
    property  OnStartDrag;
    property Playing: WordBool index 125 read GetWordBoolProp write SetWordBoolProp stored False;
    property Quality: Integer index 105 read GetIntegerProp write SetIntegerProp stored False;
    property ScaleMode: Integer index 120 read GetIntegerProp write SetIntegerProp stored False;
    property AlignMode: Integer index 121 read GetIntegerProp write SetIntegerProp stored False;
    property BackgroundColor: Integer index 123 read GetIntegerProp write SetIntegerProp stored False;
    property Loop: WordBool index 106 read GetWordBoolProp write SetWordBoolProp stored False;
    property Movie: WideString index 102 read GetWideStringProp write SetWideStringProp stored False;
    property FrameNum: Integer index 107 read GetIntegerProp write SetIntegerProp stored False;
    property WMode: WideString index 133 read GetWideStringProp write SetWideStringProp stored False;
    property SAlign: WideString index 134 read GetWideStringProp write SetWideStringProp stored False;
    property Menu: WordBool index 135 read GetWordBoolProp write SetWordBoolProp stored False;
    property Base: WideString index 136 read GetWideStringProp write SetWideStringProp stored False;
    property Scale: WideString index 137 read GetWideStringProp write SetWideStringProp stored False;
    property DeviceFont: WordBool index 138 read GetWordBoolProp write SetWordBoolProp stored False;
    property EmbedMovie: WordBool index 139 read GetWordBoolProp write SetWordBoolProp stored False;
    property BGColor: WideString index 140 read GetWideStringProp write SetWideStringProp stored False;
    property Quality2: WideString index 141 read GetWideStringProp write SetWideStringProp stored False;
    property SWRemote: WideString index 159 read GetWideStringProp write SetWideStringProp stored False;
    property FlashVars: WideString index 170 read GetWideStringProp write SetWideStringProp stored False;
    property AllowScriptAccess: WideString index 171 read GetWideStringProp write SetWideStringProp stored False;
    property MovieData: WideString index 190 read GetWideStringProp write SetWideStringProp stored False;
    property SeamlessTabbing: WordBool index 192 read GetWordBoolProp write SetWordBoolProp stored False;
    property Profile: WordBool index 194 read GetWordBoolProp write SetWordBoolProp stored False;
    property ProfileAddress: WideString index 195 read GetWideStringProp write SetWideStringProp stored False;
    property ProfilePort: Integer index 196 read GetIntegerProp write SetIntegerProp stored False;
    property AllowNetworking: WideString index 201 read GetWideStringProp write SetWideStringProp stored False;
    property AllowFullScreen: WideString index 202 read GetWideStringProp write SetWideStringProp stored False;
    property AllowFullScreenInteractive: WideString index 501 read GetWideStringProp write SetWideStringProp stored False;
    property IsDependent: WordBool index 502 read GetWordBoolProp write SetWordBoolProp stored False;
    property OnReadyStateChange: TShockwaveFlashOnReadyStateChange read FOnReadyStateChange write FOnReadyStateChange;
    property OnProgress: TShockwaveFlashOnProgress read FOnProgress write FOnProgress;
    property OnFSCommand: TShockwaveFlashFSCommand read FOnFSCommand write FOnFSCommand;
    property OnFlashCall: TShockwaveFlashFlashCall read FOnFlashCall write FOnFlashCall;
  end;

// *********************************************************************//
// The Class CoFlashObjectInterface provides a Create and CreateRemote method to          
// create instances of the default interface IFlashObjectInterface exposed by              
// the CoClass FlashObjectInterface. The functions are intended to be used by             
// clients wishing to automate the CoClass objects exposed by the         
// server of this typelibrary.                                            
// *********************************************************************//
  CoFlashObjectInterface = class
    class function Create: IFlashObjectInterface;
    class function CreateRemote(const MachineName: string): IFlashObjectInterface;
  end;

// *********************************************************************//
// The Class CoFlashObject provides a Create and CreateRemote method to          
// create instances of the default interface IFlashObject exposed by              
// the CoClass FlashObject. The functions are intended to be used by             
// clients wishing to automate the CoClass objects exposed by the         
// server of this typelibrary.                                            
// *********************************************************************//
  CoFlashObject = class
    class function Create: IFlashObject;
    class function CreateRemote(const MachineName: string): IFlashObject;
  end;

procedure Register;

resourcestring
  dtlServerPage = 'ActiveX';

  dtlOcxPage = 'ActiveX';

implementation

uses ComObj;

procedure TShockwaveFlash.InitControlData;
const
  CEventDispIDs: array [0..3] of DWORD = (
    $FFFFFD9F, $000007A6, $00000096, $000000C5);
  CControlData: TControlData2 = (
    ClassID: '{D27CDB6E-AE6D-11CF-96B8-444553540000}';
    EventIID: '{D27CDB6D-AE6D-11CF-96B8-444553540000}';
    EventCount: 4;
    EventDispIDs: @CEventDispIDs;
    LicenseKey: nil (*HR:$80004002*);
    Flags: $00000000;
    Version: 401);
begin
  ControlData := @CControlData;
  TControlData2(CControlData).FirstEventOfs := Cardinal(@@FOnReadyStateChange) - Cardinal(Self);
end;

procedure TShockwaveFlash.CreateControl;

  procedure DoCreate;
  begin
    FIntf := IUnknown(OleObject) as IShockwaveFlash;
  end;

begin
  if FIntf = nil then DoCreate;
end;

function TShockwaveFlash.GetControlInterface: IShockwaveFlash;
begin
  CreateControl;
  Result := FIntf;
end;

function TShockwaveFlash.Get_InlineData: IUnknown;
begin
    Result := DefaultInterface.InlineData;
end;

procedure TShockwaveFlash.Set_InlineData(const ppIUnknown: IUnknown);
begin
  DefaultInterface.Set_InlineData(ppIUnknown);
end;

procedure TShockwaveFlash.SetZoomRect(left: Integer; top: Integer; right: Integer; bottom: Integer);
begin
  DefaultInterface.SetZoomRect(left, top, right, bottom);
end;

procedure TShockwaveFlash.Zoom(factor: SYSINT);
begin
  DefaultInterface.Zoom(factor);
end;

procedure TShockwaveFlash.Pan(x: Integer; y: Integer; mode: SYSINT);
begin
  DefaultInterface.Pan(x, y, mode);
end;

procedure TShockwaveFlash.Play;
begin
  DefaultInterface.Play;
end;

procedure TShockwaveFlash.Stop;
begin
  DefaultInterface.Stop;
end;

procedure TShockwaveFlash.Back;
begin
  DefaultInterface.Back;
end;

procedure TShockwaveFlash.Forward;
begin
  DefaultInterface.Forward;
end;

procedure TShockwaveFlash.Rewind;
begin
  DefaultInterface.Rewind;
end;

procedure TShockwaveFlash.StopPlay;
begin
  DefaultInterface.StopPlay;
end;

procedure TShockwaveFlash.GotoFrame(FrameNum: Integer);
begin
  DefaultInterface.GotoFrame(FrameNum);
end;

function TShockwaveFlash.CurrentFrame: Integer;
begin
  Result := DefaultInterface.CurrentFrame;
end;

function TShockwaveFlash.IsPlaying: WordBool;
begin
  Result := DefaultInterface.IsPlaying;
end;

function TShockwaveFlash.PercentLoaded: Integer;
begin
  Result := DefaultInterface.PercentLoaded;
end;

function TShockwaveFlash.FrameLoaded(FrameNum: Integer): WordBool;
begin
  Result := DefaultInterface.FrameLoaded(FrameNum);
end;

function TShockwaveFlash.FlashVersion: Integer;
begin
  Result := DefaultInterface.FlashVersion;
end;

procedure TShockwaveFlash.LoadMovie(layer: SYSINT; const url: WideString);
begin
  DefaultInterface.LoadMovie(layer, url);
end;

procedure TShockwaveFlash.TGotoFrame(const target: WideString; FrameNum: Integer);
begin
  DefaultInterface.TGotoFrame(target, FrameNum);
end;

procedure TShockwaveFlash.TGotoLabel(const target: WideString; const label_: WideString);
begin
  DefaultInterface.TGotoLabel(target, label_);
end;

function TShockwaveFlash.TCurrentFrame(const target: WideString): Integer;
begin
  Result := DefaultInterface.TCurrentFrame(target);
end;

function TShockwaveFlash.TCurrentLabel(const target: WideString): WideString;
begin
  Result := DefaultInterface.TCurrentLabel(target);
end;

procedure TShockwaveFlash.TPlay(const target: WideString);
begin
  DefaultInterface.TPlay(target);
end;

procedure TShockwaveFlash.TStopPlay(const target: WideString);
begin
  DefaultInterface.TStopPlay(target);
end;

procedure TShockwaveFlash.SetVariable(const name: WideString; const value: WideString);
begin
  DefaultInterface.SetVariable(name, value);
end;

function TShockwaveFlash.GetVariable(const name: WideString): WideString;
begin
  Result := DefaultInterface.GetVariable(name);
end;

procedure TShockwaveFlash.TSetProperty(const target: WideString; property_: SYSINT; 
                                       const value: WideString);
begin
  DefaultInterface.TSetProperty(target, property_, value);
end;

function TShockwaveFlash.TGetProperty(const target: WideString; property_: SYSINT): WideString;
begin
  Result := DefaultInterface.TGetProperty(target, property_);
end;

procedure TShockwaveFlash.TCallFrame(const target: WideString; FrameNum: SYSINT);
begin
  DefaultInterface.TCallFrame(target, FrameNum);
end;

procedure TShockwaveFlash.TCallLabel(const target: WideString; const label_: WideString);
begin
  DefaultInterface.TCallLabel(target, label_);
end;

procedure TShockwaveFlash.TSetPropertyNum(const target: WideString; property_: SYSINT; value: Double);
begin
  DefaultInterface.TSetPropertyNum(target, property_, value);
end;

function TShockwaveFlash.TGetPropertyNum(const target: WideString; property_: SYSINT): Double;
begin
  Result := DefaultInterface.TGetPropertyNum(target, property_);
end;

function TShockwaveFlash.TGetPropertyAsNumber(const target: WideString; property_: SYSINT): Double;
begin
  Result := DefaultInterface.TGetPropertyAsNumber(target, property_);
end;

procedure TShockwaveFlash.EnforceLocalSecurity;
begin
  DefaultInterface.EnforceLocalSecurity;
end;

function TShockwaveFlash.CallFunction(const request: WideString): WideString;
begin
  Result := DefaultInterface.CallFunction(request);
end;

procedure TShockwaveFlash.SetReturnValue(const returnValue: WideString);
begin
  DefaultInterface.SetReturnValue(returnValue);
end;

procedure TShockwaveFlash.DisableLocalSecurity;
begin
  DefaultInterface.DisableLocalSecurity;
end;

class function CoFlashObjectInterface.Create: IFlashObjectInterface;
begin
  Result := CreateComObject(CLASS_FlashObjectInterface) as IFlashObjectInterface;
end;

class function CoFlashObjectInterface.CreateRemote(const MachineName: string): IFlashObjectInterface;
begin
  Result := CreateRemoteComObject(MachineName, CLASS_FlashObjectInterface) as IFlashObjectInterface;
end;

class function CoFlashObject.Create: IFlashObject;
begin
  Result := CreateComObject(CLASS_FlashObject) as IFlashObject;
end;

class function CoFlashObject.CreateRemote(const MachineName: string): IFlashObject;
begin
  Result := CreateRemoteComObject(MachineName, CLASS_FlashObject) as IFlashObject;
end;

procedure Register;
begin
  RegisterComponents(dtlOcxPage, [TShockwaveFlash]);
end;

end.
