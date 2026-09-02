; ================================================
; PSI Cashier 安装包配置
; 使用 Inno Setup 编译
; ================================================

#define MyAppName "PSI Cashier"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "PSI Systems"
#define MyAppExeName "start.bat"
#define MyAppId "{{{1E8F6E7D-4A3B-4C9D-8E7F-3A2B1C4D5E6F}"

[Setup]
AppId={#MyAppId}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
OutputDir=..\installer
OutputBaseFilename=PSI_Setup
SetupIconFile=psi-icon.ico
Compression=lzma2/ultra64
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "english"; MessagesFile: "compiler:Languages\English.isl"
Name: "chinesesimplified"; MessagesFile: "compiler:Languages\ChineseSimplified.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; 绿色 JRE（约 100MB）
Source: "package\jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs

; 应用程序 JAR
Source: "package\psi-cashier.jar"; DestDir: "{app}"; Flags: ignoreversion

; 配置文件
Source: "package\application.properties"; DestDir: "{app}"; Flags: ignoreversion

; 启动脚本
Source: "package\start.bat"; DestDir: "{app}"; Flags: ignoreversion

; 数据目录（空目录，运行时创建数据库）
Source: "package\data\*"; DestDir: "{app}\data"; Flags: ignoreversion recursesubdirs createallsubdirs

; 备份目录（空目录）
Source: "package\backup\*"; DestDir: "{app}\backup"; Flags: ignoreversion recursesubdirs createallsubdirs

; 日志目录（空目录）
Source: "package\logs\*"; DestDir: "{app}\logs"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{group}\{cm:UninstallProgram,{#StringChange(MyAppName, '&', '&&')}}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
Type: filesandordirs; Name: "{app}\data"
Type: filesandordirs; Name: "{app}\backup"
Type: filesandordirs; Name: "{app}\logs"

[Code]
procedure CurStepChanged(CurStep: TSetupStep);
var
  ResultCode: Integer;
begin
  if CurStep = ssPostInstall then
  begin
    // 创建启动器 EXE（使用批处理转换为 EXE）
    // 实际上这里应该在打包时就准备好 PSI Cashier.exe 启动器
    // 或者使用 Inno Setup 的 Exec 直接启动 Java
  end;
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
begin
  if CurUninstallStep = usPostUninstall then
  begin
    // 删除应用数据目录（可选，让用户选择是否保留数据）
  end;
end;
