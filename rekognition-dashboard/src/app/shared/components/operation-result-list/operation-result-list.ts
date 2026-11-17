import { Component, Input } from '@angular/core';
import { IonList } from '@ionic/angular';
import { NgFor, NgIf } from '@angular/common';
import { OperationResultItem } from '../operation-result-item/operation-result-item';

export interface OperationResult {
  type: string;
  status: string;
  datetime: string;
  message: string;
}

@Component({
  selector: 'app-operation-result-list',
  standalone: true,
  imports: [IonList, NgFor, NgIf, OperationResultItem],
  templateUrl: './operation-result-list.html',
  styleUrls: ['./operation-result-list.scss'],
})
export class OperationResultList {
  readonly emptyResult: OperationResult = {
    type: 'Sin pruebas',
    status: 'info',
    datetime: '',
    message: 'Aún no se han realizado pruebas.',
  };

  @Input() results: OperationResult[] = [];
}