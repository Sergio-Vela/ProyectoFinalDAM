import { Component } from '@angular/core';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonIcon,
  IonButton,
} from '@ionic/angular';
import { NativeTools } from 'capacitor-native-tools';
import { AwsLivenessService, GetResultsResponse } from '../services/aws-liveness.service';

type LivenessStatus = 'idle' | 'creating' | 'running' | 'fetching' | 'complete' | 'error';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonIcon,
    IonButton,
  ],
})
export class HomePage {
  status: LivenessStatus = 'idle';
  sessionId: string | null = null;
  result: GetResultsResponse | null = null;
  error: string | null = null;

  constructor(private livenessService: AwsLivenessService) {}

  get isBusy(): boolean {
    return (
      this.status === 'creating' ||
      this.status === 'running' ||
      this.status === 'fetching'
    );
  }

  get statusText(): string {
    switch (this.status) {
      case 'creating':
        return 'Creando sesión de Face Liveness...';
      case 'running':
        return 'Esperando verificación facial...';
      case 'fetching':
        return 'Consultando resultados...';
      case 'complete':
        return 'Verificación completada';
      case 'error':
        return 'Ocurrió un error';
      default:
        return 'Listo para verificar';
    }
  }

  async iniciarVerificacion(): Promise<void> {
    if (this.isBusy) {
      return;
    }

    this.status = 'creating';
    this.result = null;
    this.error = null;

    try {
      // 1. Solicitar creación de sesión al backend (Lambda existente)
      const session = await this.livenessService.createSession().toPromise();
      if (!session) {
        throw new Error('El backend no devolvió un sessionId válido.');
      }

      this.sessionId = session;
      console.log('SessionId recibido:', this.sessionId);

      // 2. Invocar plugin Capacitor con el SessionId
      this.status = 'running';
      const pluginResult = await NativeTools.startFaceLiveness({
        sessionId: this.sessionId,
        region: 'us-east-1',
      });

      console.log('Resultado del plugin:', pluginResult);

      if (pluginResult.status === 'cancelled') {
        this.status = 'idle';
        this.error = 'Cancelaste la verificación facial.';
        return;
      }

      if (pluginResult.status !== 'complete') {
        throw new Error(
          pluginResult.error || 'La verificación facial no se completó correctamente.'
        );
      }

      // 3. Consultar resultados al backend (Lambda existente)
      this.status = 'fetching';
      const results = await this.livenessService.getSessionResults(this.sessionId).toPromise();
      if (!results) {
        throw new Error('El backend no devolvió resultados.');
      }

      this.result = results;
      this.status = 'complete';
    } catch (e) {
      console.error(e);
      this.status = 'error';
      this.error =
        e instanceof Error ? e.message : 'Ocurrió un error inesperado.';
    }
  }

  get confidencePercentage(): string {
    if (this.result?.confidenceScore == null) {
      return '—';
    }
    return `${(this.result.confidenceScore * 100).toFixed(1)}%`;
  }

  get isLiveLabel(): string {
    if (this.result?.isLive == null) {
      return '—';
    }
    return this.result.isLive ? 'Sí' : 'No';
  }
}