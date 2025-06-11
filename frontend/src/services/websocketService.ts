interface PriceUpdate {
  symbol: string;
  price: number;
  volume?: number;
  change24h?: number;
  exchange: string;
  timestamp: number;
}

interface WebSocketMessage {
  type: string;
  [key: string]: any;
}

type PriceUpdateCallback = (symbol: string, data: PriceUpdate) => void;
type ConnectionCallback = (connected: boolean) => void;

class WebSocketService {
  private socket: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectInterval = 1000;
  private isConnecting = false;
  private subscriptions = new Set<string>();
  private priceUpdateCallbacks = new Set<PriceUpdateCallback>();
  private connectionCallbacks = new Set<ConnectionCallback>();

  constructor() {
    this.connect();
  }

  private connect() {
    if (this.isConnecting || (this.socket && this.socket.readyState === WebSocket.CONNECTING)) {
      return;
    }

    this.isConnecting = true;
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
    const wsUrl = apiUrl.replace('http://', 'ws://').replace('/api', '/ws/prices');
    
    try {
      this.socket = new WebSocket(wsUrl);
      
      this.socket.onopen = () => {
        console.log('WebSocket connected to price updates');
        this.isConnecting = false;
        this.reconnectAttempts = 0;
        this.notifyConnectionCallbacks(true);
        
        // Re-subscribe to all symbols after reconnection
        if (this.subscriptions.size > 0) {
          this.resubscribeAll();
        }
      };

      this.socket.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data);
          this.handleMessage(message);
        } catch (error) {
          console.error('Error parsing WebSocket message:', error);
        }
      };

      this.socket.onclose = () => {
        console.log('WebSocket connection closed');
        this.isConnecting = false;
        this.notifyConnectionCallbacks(false);
        this.handleReconnect();
      };

      this.socket.onerror = (error) => {
        console.error('WebSocket error:', error);
        this.isConnecting = false;
      };
      
    } catch (error) {
      console.error('Failed to create WebSocket connection:', error);
      this.isConnecting = false;
      this.handleReconnect();
    }
  }

  private handleMessage(message: WebSocketMessage) {
    switch (message.type) {
      case 'welcome':
        console.log('WebSocket welcome message:', message.message);
        break;
      case 'subscription_confirmed':
        console.log('Subscription confirmed for symbols:', message.symbols);
        break;
      case 'unsubscription_confirmed':
        console.log('Unsubscription confirmed for symbols:', message.symbols);
        break;
      case 'price_update':
        this.handlePriceUpdate(message);
        break;
      case 'pong':
        console.log('Received pong from server');
        break;
      case 'error':
        console.error('WebSocket error:', message.message);
        break;
      default:
        console.log('Unknown message type:', message.type);
    }
  }

  private handlePriceUpdate(message: WebSocketMessage) {
    const { symbol, data } = message;
    
    if (symbol && data) {
      const priceUpdate: PriceUpdate = {
        symbol: symbol.toUpperCase(),
        price: data.price,
        volume: data.volume,
        change24h: data.change24h,
        exchange: data.exchange,
        timestamp: data.timestamp
      };
      
      this.priceUpdateCallbacks.forEach(callback => {
        try {
          callback(symbol, priceUpdate);
        } catch (error) {
          console.error('Error in price update callback:', error);
        }
      });
    }
  }

  private handleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('Max reconnection attempts reached');
      return;
    }

    this.reconnectAttempts++;
    const delay = this.reconnectInterval * Math.pow(2, this.reconnectAttempts - 1);
    
    console.log(`Attempting to reconnect in ${delay}ms (attempt ${this.reconnectAttempts})`);
    
    setTimeout(() => {
      this.connect();
    }, delay);
  }

  private resubscribeAll() {
    if (this.subscriptions.size > 0) {
      const symbols = Array.from(this.subscriptions);
      this.sendMessage({
        action: 'subscribe',
        symbols: symbols
      });
    }
  }

  private sendMessage(message: any) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(message));
    } else {
      console.warn('WebSocket is not connected, message not sent:', message);
    }
  }

  private notifyConnectionCallbacks(connected: boolean) {
    this.connectionCallbacks.forEach(callback => {
      try {
        callback(connected);
      } catch (error) {
        console.error('Error in connection callback:', error);
      }
    });
  }

  // Public API
  subscribe(symbols: string | string[]) {
    const symbolArray = Array.isArray(symbols) ? symbols : [symbols];
    const normalizedSymbols = symbolArray.map(s => s.toUpperCase());
    
    normalizedSymbols.forEach(symbol => this.subscriptions.add(symbol));
    
    this.sendMessage({
      action: 'subscribe',
      symbols: normalizedSymbols
    });
  }

  unsubscribe(symbols: string | string[]) {
    const symbolArray = Array.isArray(symbols) ? symbols : [symbols];
    const normalizedSymbols = symbolArray.map(s => s.toUpperCase());
    
    normalizedSymbols.forEach(symbol => this.subscriptions.delete(symbol));
    
    this.sendMessage({
      action: 'unsubscribe',
      symbols: normalizedSymbols
    });
  }

  ping() {
    this.sendMessage({ action: 'ping' });
  }

  onPriceUpdate(callback: PriceUpdateCallback) {
    this.priceUpdateCallbacks.add(callback);
    
    return () => {
      this.priceUpdateCallbacks.delete(callback);
    };
  }

  onConnectionChange(callback: ConnectionCallback) {
    this.connectionCallbacks.add(callback);
    
    return () => {
      this.connectionCallbacks.delete(callback);
    };
  }

  getConnectionStatus(): boolean {
    return this.socket?.readyState === WebSocket.OPEN;
  }

  getSubscriptions(): string[] {
    return Array.from(this.subscriptions);
  }

  disconnect() {
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
    this.subscriptions.clear();
    this.priceUpdateCallbacks.clear();
    this.connectionCallbacks.clear();
  }
}

// Export singleton instance
export const webSocketService = new WebSocketService();
export type { PriceUpdate, PriceUpdateCallback, ConnectionCallback }; 