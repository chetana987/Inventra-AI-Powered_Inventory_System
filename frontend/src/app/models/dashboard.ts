export interface LowStockProduct {
  id: number;
  productCode: string;
  name: string;
  category: string;
  quantity: number;
  minimumStockLevel: number;
}

export interface RecentTransaction {
  id: number;
  productId: number;
  productCode: string;
  productName: string;
  transactionType: 'STOCK_IN' | 'STOCK_OUT';
  quantity: number;
  remarks: string;
  transactionDate: string;
  performedBy: string;
}

export interface CategoryDistribution {
  category: string;
  count: number;
  totalStock: number;
  percentage: number;
}

export interface ChartDataPoint {
  year: number;
  month: number;
  monthLabel: string;
  inCount: number;
  outCount: number;
  inQuantity: number;
  outQuantity: number;
}

export interface DashboardData {
  totalProducts: number;
  totalStockQuantity: number;
  lowStockCount: number;
  lowStockProducts: LowStockProduct[];
  recentTransactions: RecentTransaction[];
  inventoryDistribution: CategoryDistribution[];
  monthlyTransactions: ChartDataPoint[];
  stockMovementTrends: ChartDataPoint[];
}
