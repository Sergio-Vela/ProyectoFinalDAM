import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

/** 
 * Servicio HTTP para comunicarse con las Lambda Functions existentes. 
 * Shapes de respuesta basados en apps/aws/ (no modificar las Lambdas): 
 * - createSession (GET): string puro JSON \"sessionId\" 
 * - getResults (POST {sessionId}): { confidence, status, referenceImageBase64 } 
 * - compareFaces (POST {sourceImageBase64, targetImageBase64}): { confidence, status, referenceImageBase64 } 
 */ 
@Injectable({
  providedIn: 'root',
})
export class AwsLivenessService {
  // URLs de las Lambda Functions (configuradas en environment.ts)
  private readonly createSessionUrl =
    'https://oqmf46yrztqauqzv3ymnhtgnku0kxntp.lambda-url.us-east-1.on.aws/';
  private readonly getResultsUrl =
    'https://z6shsqj6bwfyoaoscoefni2cpi0licem.lambda-url.us-east-1.on.aws/';
  private readonly compareFacesUrl =
    'https://djttxus37u76gku7zpoogalpeq0skdkq.lambda-url.us-east-1.on.aws/';

  constructor(private http: HttpClient) {}

  /** 
   * Crea una nueva sesión de Face Liveness.
   * La Lambda devuelve un string JSON como \"98770a62-8312-412f-bc7b-31c4613ae370\". 
   * Usamos responseType: 'text' para obtenerlo tal cual, y lo parseamos manualmente.
   */
  createSession(): Observable<string> {
    return this.http
      .get(this.createSessionUrl, { responseType: 'text' })
      .pipe(
        map((res: string) => {
          const trimmed = res.trim();
          console.log('[AwsLivenessService] createSession response text:', trimmed, 'longitud:', trimmed.length);
          // La Lambda devuelve el sessionId como string JSON: \"abc-123\"
          // El string incluye las comillas, así que quitamos las comillas externas si existen
          let sessionId = trimmed;
          if ((sessionId.startsWith('"') && sessionId.endsWith('"')) || 
              (sessionId.startsWith("'") && sessionId.endsWith("'"))) {
            sessionId = sessionId.slice(1, -1);
            console.log('[AwsLivenessService] sessionId quitando comillas:', sessionId);
          }
          
          if (sessionId.length === 0) {
            console.error('[AwsLivenessService] Respuesta inválida: sessionId vacío después de procesar');
            throw new Error('Respuesta inválida: falta sessionId');
          }
          
          console.log('[AwsLivenessService] sessionId final:', sessionId);
          return sessionId;
        }),
        catchError((error) => {
          console.error('[AwsLivenessService] createSession error:', error);
          return of('');
        })
      );
  }

  /** 
   * Consulta los resultados de una sesión Face Liveness.
   * Se envía POST con body { sessionId: \"...\" }.
   * La Lambda devuelve { confidence, status, referenceImageBase64 }.
   * Mapeamos a GetResultsResponse (status, confidenceScore, isLive, message).
   */
  getSessionResults(sessionId: string): Observable<GetResultsResponse> {
    return this.http
      .post<LambdaResultsResponse>(this.getResultsUrl, { sessionId })
      .pipe(
        map((res) => {
          console.log('[AwsLivenessService] getSessionResults response:', res);
          if (!res) {
            throw new Error('Respuesta vacía del backend');
          }
          const status = res.status ?? 'unknown';
          const confidenceScore = typeof res.confidence === 'number' 
            ? res.confidence 
            : typeof res.confidence === 'string' 
              ? parseFloat(res.confidence) 
              : undefined;
          
          const isLive = status === 'SUCCESS';
          const message = res.message ?? 'Resultados recibidos';
          return {
            status,
            confidenceScore,
            isLive,
            message,
          };
        }),
        catchError((error) => {
          console.error('[AwsLivenessService] getSessionResults error:', error);
          return of({ status: 'error', message: error instanceof Error ? error.message : 'Error desconocido' });
        })
      );
  }

  /** 
   * Compara dos imágenes mediante Amazon Rekognition CompareFaces.
   * Se envía POST con body { sourceImageBase64, targetImageBase64 }.
   * La Lambda devuelve { confidence, status, referenceImageBase64 }.
   */
  compareFaces(sourceImageBase64: string, targetImageBase64: string): Observable<CompareFacesResponse> {
    return this.http
      .post<LambdaResultsResponse>(this.compareFacesUrl, {
        sourceImageBase64,
        targetImageBase64,
      })
      .pipe(
        map((res) => {
          console.log('[AwsLivenessService] compareFaces response:', res);
          if (!res) {
            throw new Error('Respuesta vacía del backend');
          }
          const similarity = typeof res.confidence === 'number'
            ? res.confidence
            : typeof res.confidence === 'string'
              ? parseFloat(res.confidence)
              : undefined;
          const message = res.message ?? 'Comparación completada';
          return {
            similarity,
            message,
          };
        }),
        catchError((error) => {
          console.error('[AwsLivenessService] compareFaces error:', error);
          return of({ similarity: undefined, message: error instanceof Error ? error.message : 'Error desconocido' });
        })
      );
  }
} 

/** 
 * Interfaces de respuesta basadas en apps/aws/ 
 */ 
export interface CreateSessionResponse {
  sessionId: string;
}

export interface GetResultsResponse {
  status: string;
  confidenceScore?: number;
  isLive?: boolean;
  message?: string;
}

export interface CompareFacesResponse {
  similarity?: number;
  message?: string;
}

/** Respuesta cruda de las Lambdas 2 y 3 (tal como la devuelven hoy). */
export interface LambdaResultsResponse {
  confidence?: number | string;
  status?: string;
  referenceImageBase64?: string;
  message?: string;
}