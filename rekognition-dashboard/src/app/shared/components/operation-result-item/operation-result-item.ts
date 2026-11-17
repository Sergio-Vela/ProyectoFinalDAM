import { Component, Input } from '@angular/core';
import { IonCard, IonLabel, IonNote, IonBadge } from '@ionic/angular';

interface OperationResult {
  type: string;
  status: string;
  datetime: string;
  message: string;
}

@Component({
  selector: 'app-operation-result-item',
  standalone: true,
  imports: [IonCard, IonLabel, IonNote, IonBadge],
  templateUrl: './operation-result-item.html',
  styleUrls: ['./operation-result-item.scss'],
})
export class OperationResultItem {
  @Input() item: OperationResult = { type: '', status: 'info', datetime: '', message: '' };
}