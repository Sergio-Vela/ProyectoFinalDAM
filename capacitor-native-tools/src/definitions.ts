export interface NativeToolsPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  showNativeAlert(): Promise<{ action: string }>;
}
