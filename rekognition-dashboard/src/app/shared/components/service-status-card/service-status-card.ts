import { Component, Input } from '@angular/core';
import { IonCard, IonCardHeader, IonCardTitle, IonCardSubtitle, IonBadge, IonIcon } from '@ionic/angular';

type StatusVariant = 'pending' | 'success' | 'error';

@Component({
  selector: 'app-service-status-card',
  standalone: true,
  imports: [IonCard, IonCardHeader, IonCardTitle, IonCardSubtitle, IonBadge, IonIcon],
  templateUrl: './service-status-card.html',
  styleUrls: ['./service-status-card.scss'],
})
export class ServiceStatusCard {
  @Input() icon = '';
  @Input() title = '';
  @Input() description = '';
  @Input() statusLabel = '';
  @Input() statusVariant: StatusVariant = 'pending';
}