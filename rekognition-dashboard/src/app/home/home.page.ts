import { Component } from '@angular/core';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonIcon,
  IonBadge,
  IonButton,
  IonGrid,
  IonRow,
  IonCol,
  IonCard,
  IonCardContent,
  IonLabel,
} from '@ionic/angular';
import { ServiceStatusCard } from '../shared/components/service-status-card/service-status-card';
import { OperationResultList } from '../shared/components/operation-result-list/operation-result-list';

export interface ServiceCard {
  icon: string;
  title: string;
  description: string;
  statusLabel: string;
  statusVariant: 'pending' | 'success' | 'error';
}

export interface OperationResult {
  type: string;
  status: string;
  datetime: string;
  message: string;
}

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
    IonBadge,
    IonButton,
    IonGrid,
    IonRow,
    IonCol,
    IonCard,
    IonCardContent,
    IonLabel,
    ServiceStatusCard,
    OperationResultList,
  ],
})
export class HomePage {
  services: ServiceCard[] = [
    {
      icon: 'eye-outline',
      title: 'Face Liveness',
      description: 'Verificación de identidad mediante desafíos faciales en tiempo real.',
      statusLabel: 'Pendiente de integración',
      statusVariant: 'pending',
    },
    {
      icon: 'list-outline',
      title: 'Consulta de resultados',
      description: 'Consulta de resultados Face Liveness mediante Lambda.',
      statusLabel: 'Pendiente de integración',
      statusVariant: 'pending',
    },
    {
      icon: 'swap-horizontal-outline',
      title: 'Comparación facial',
      description: 'Comparación de rostros mediante Amazon Rekognition CompareFaces.',
      statusLabel: 'Pendiente de integración',
      statusVariant: 'pending',
    },
  ];

  results: OperationResult[] = [];

  notImplemented() {
    // Sin integración todavía: se mantiene solo como marcador visual.
  }
}