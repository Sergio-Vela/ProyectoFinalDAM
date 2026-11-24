import { Component, OnInit } from '@angular/core';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonIcon,
  IonButton,
  IonSpinner,
  IonText,
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
    IonSpinner,
  ],
})
export class HomePage implements OnInit {
  status: LivenessStatus = 'idle';
  sessionId: string | null = null;
  result: GetResultsResponse | null = null;
  error: string | null = null;

  // Timeouts de seguridad (ms)
  private readonly PLUGIN_TIMEOUT_MS = 60000;  // 60s para FaceLivenessActivity
  private readonly LAMBDA_TIMEOUT_MS = 30000;  // 30s para peticiones HTTP

  constructor(private livenessService: AwsLivenessService) {}

  ngOnInit() {
    // Ejecutar automáticamente al cargar la página
    this.iniciarVerificacion();
  }

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
        return 'Abriendo verificación facial...';
      case 'fetching':
        return 'Consultando resultados...';
      case 'complete':
        return 'Verificación completada';
      case 'error':
        return 'Ocurrió un error';
      default:
        return 'Iniciando verificación...';
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
      console.log('[HomePage] Solicitando sessionId a Lambda createSession...');
      const session = await this.withTimeout(
        this.livenessService.createSession().toPromise(),
        this.LAMBDA_TIMEOUT_MS,
        'Timeout creando sesión en Lambda'
      );
      
      if (!session) {
        throw new Error('El backend no devolvió un sessionId válido.');
      }

      this.sessionId = session;
      console.log('[HomePage] SessionId recibido:', this.sessionId);

      // 2. Invocar plugin Capacitor con el SessionId (con timeout de seguridad)
      this.status = 'running';
      console.log('[HomePage] Lanzando FaceLivenessActivity via plugin...');
      
      const pluginResult = await this.withTimeout(
        NativeTools.startFaceLiveness({
          sessionId: this.sessionId,
          region: 'us-east-1',
        }),
        this.PLUGIN_TIMEOUT_MS,
        'Timeout esperando FaceLivenessActivity (¿permiso de cámara?)'
      );

      console.log('[HomePage] Resultado del plugin:', pluginResult);

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
      console.log('[HomePage] Consultando resultados a Lambda getSessionResults...');
      const results = await this.withTimeout(
        this.livenessService.getSessionResults(this.sessionId).toPromise(),
        this.LAMBDA_TIMEOUT_MS,
        'Timeout consultando resultados en Lambda'
      );
      
      if (!results) {
        throw new Error('El backend no devolvió resultados.');
      }

      this.result = results;
      this.status = 'complete';
      console.log('[HomePage] Verificación completada con éxito');
    } catch (e) {
      console.error('[HomePage] Error en verificación:', e);
      this.status = 'error';
      this.error =
        e instanceof Error ? e.message : 'Ocurrió un error inesperado.';
    }
  }

  /**
   * Wrapper con timeout para promesas. Evita que la UI se quede colgada "Esperando...".
   */
  private async withTimeout<T>(
    promise: Promise<T>,
    timeoutMs: number,
    timeoutMessage: string
  ): Promise<T> {
    return Promise.race([
      promise,
      new Promise<never>((_, reject) =>
        setTimeout(() => reject(new Error(timeoutMessage)), timeoutMs)
      ),
    ]);
  }

  reintentar() {
    this.iniciarVerificacion();
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