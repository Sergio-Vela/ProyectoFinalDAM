import { WebPlugin } from '@capacitor/core';

import type { NativeToolsPlugin } from './definitions';

export class NativeToolsWeb extends WebPlugin implements NativeToolsPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async showNativeAlert(): Promise<{ action: string }> {
    throw new Error('La alerta nativa solo está disponible en Android');
  }

  async startFaceLiveness(_options: {
    sessionId: string;
    region?: string;
  }): Promise<{ status: 'complete' | 'error' | 'cancelled'; result?: string; error?: string }> {
    throw new Error('Face Liveness solo está disponible en Android');
  }
}