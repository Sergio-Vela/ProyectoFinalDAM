export interface NativeToolsPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  showNativeAlert(): Promise<{ action: string }>;
  startFaceLiveness(options: { sessionId: string; region?: string }): Promise<{
    status: 'complete' | 'error' | 'cancelled';
    result?: string;
    error?: string;
  }>;
}