import { TransactionType } from './enums';

export interface InventoryRequest {
  productId: number;
  quantity: number;
  remarks?: string;
}

export interface InventoryResponse {
  id: number;
  productId: number;
  productCode: string;
  productName: string;
  transactionType: TransactionType;
  quantity: number;
  remarks: string;
  transactionDate: string;
  performedBy: string;
}
