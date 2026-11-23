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
}
