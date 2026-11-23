import { registerPlugin } from '@capacitor/core';

import type { NativeToolsPlugin } from './definitions';

const NativeTools = registerPlugin<NativeToolsPlugin>('NativeTools', {
  web: () => import('./web').then((m) => new m.NativeToolsWeb()),
});

export * from './definitions';
export { NativeTools };
