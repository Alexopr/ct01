import React, { useState, useEffect, useCallback } from "react";
import { Icon } from "@iconify/react";
import { webSocketService } from "../services/websocketService";
import type { PriceUpdate } from "../services/websocketService";
import { apiService } from "../services/api";
import type { CryptoTickerData, CryptoTickerSettings } from "../types/api";

const CryptoTicker: React.FC = () => {
  const [isPaused, setIsPaused] = useState(false);
  const [cryptoData, setCryptoData] = useState<CryptoTickerData[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const [settings, setSettings] = useState<CryptoTickerSettings>({
    enabled: true,
    selectedCoins: ['BTC', 'ETH', 'SOL'],
    exchange: 'bybit',
    animationSpeed: 'medium',
    showChangePercent: true,
  });

  // Load settings from localStorage
  useEffect(() => {
    const savedSettings = localStorage.getItem('cryptoTickerSettings');
    if (savedSettings) {
      try {
        const parsed = JSON.parse(savedSettings);
        setSettings(parsed);
      } catch (error) {
        console.error('Error loading ticker settings:', error);
      }
    }
  }, []);

  // Handle price updates from WebSocket
  const handlePriceUpdate = useCallback((symbol: string, data: PriceUpdate) => {
    setCryptoData(prevData => {
      const index = prevData.findIndex(item => item.symbol === symbol);
      const newItem: CryptoTickerData = {
        symbol: data.symbol,
        name: getCoinName(data.symbol),
        price: data.price,
        change24h: data.change24h || 0,
        volume24h: data.volume,
        exchange: data.exchange,
        timestamp: data.timestamp,
        icon: getCoinIcon(data.symbol),
        exchangeUrl: getExchangeUrl(data.symbol, settings.exchange)
      };

      if (index >= 0) {
        const newData = [...prevData];
        newData[index] = newItem;
        return newData;
      } else {
        return [...prevData, newItem];
      }
    });
  }, [settings.exchange]);

  // Handle WebSocket connection status
  const handleConnectionChange = useCallback((connected: boolean) => {
    setIsConnected(connected);
  }, []);

  // Initialize data and WebSocket connection
  useEffect(() => {
    if (!settings.enabled || settings.selectedCoins.length === 0) {
      return;
    }

    const initializeData = async () => {
      try {
        // Load initial price data
        const initialData: CryptoTickerData[] = [];
        
        for (const symbol of settings.selectedCoins) {
          try {
            const priceData = await apiService.getCoinPrice(symbol);
            
            if (priceData && priceData.price) {
              initialData.push({
                symbol: symbol,
                name: getCoinName(symbol),
                price: priceData.price,
                change24h: priceData.change24h || 0,
                volume24h: priceData.volume24h,
                exchange: priceData.exchange || settings.exchange,
                timestamp: Date.now(),
                icon: getCoinIcon(symbol),
                exchangeUrl: getExchangeUrl(symbol, settings.exchange)
              });
            }
          } catch (error) {
            console.error(`Error loading price data for ${symbol}:`, error);
            
            // Add fallback data
            initialData.push({
              symbol: symbol,
              name: getCoinName(symbol),
              price: 0,
              change24h: 0,
              exchange: settings.exchange,
              timestamp: Date.now(),
              icon: getCoinIcon(symbol),
              exchangeUrl: getExchangeUrl(symbol, settings.exchange)
            });
          }
        }
        
        setCryptoData(initialData);
        
        // Subscribe to WebSocket updates
        const unsubscribePriceUpdates = webSocketService.onPriceUpdate(handlePriceUpdate);
        const unsubscribeConnection = webSocketService.onConnectionChange(handleConnectionChange);
        
        // Subscribe to selected coins
        webSocketService.subscribe(settings.selectedCoins);
        
        return () => {
          unsubscribePriceUpdates();
          unsubscribeConnection();
          webSocketService.unsubscribe(settings.selectedCoins);
        };
        
      } catch (error) {
        console.error('Error initializing crypto ticker:', error);
      }
    };

    const cleanup = initializeData();
    
    return () => {
      cleanup.then(cleanupFn => cleanupFn && cleanupFn());
    };
  }, [settings.enabled, settings.selectedCoins, settings.exchange, handlePriceUpdate, handleConnectionChange]);

  const handleExchangeClick = (url: string, e: React.MouseEvent) => {
    e.preventDefault();
    window.open(url, '_blank');
  };

  const getAnimationSpeed = () => {
    switch (settings.animationSpeed) {
      case 'slow': return '60s';
      case 'fast': return '20s';
      default: return '40s';
    }
  };

  // Helper functions
  const getCoinName = (symbol: string): string => {
    const coinNames: Record<string, string> = {
      'BTC': 'Bitcoin',
      'ETH': 'Ethereum',
      'SOL': 'Solana',
      'ADA': 'Cardano',
      'DOT': 'Polkadot',
      'AVAX': 'Avalanche',
      'MATIC': 'Polygon',
      'LINK': 'Chainlink',
      'UNI': 'Uniswap',
      'ATOM': 'Cosmos',
      'LTC': 'Litecoin',
      'BCH': 'Bitcoin Cash'
    };
    return coinNames[symbol] || symbol;
  };

  const getCoinIcon = (symbol: string): string => {
    const coinIcons: Record<string, string> = {
      'BTC': 'logos:bitcoin',
      'ETH': 'logos:ethereum',
      'SOL': 'logos:solana',
      'ADA': 'logos:cardano',
      'DOT': 'logos:polkadot',
      'AVAX': 'logos:avalanche',
      'MATIC': 'logos:polygon',
      'LINK': 'logos:chainlink',
      'UNI': 'logos:uniswap',
      'ATOM': 'logos:cosmos',
      'LTC': 'logos:litecoin',
      'BCH': 'logos:bitcoin-cash'
    };
    return coinIcons[symbol] || 'mdi:currency-btc';
  };

  const getExchangeUrl = (symbol: string, exchange: string): string => {
    const exchangeUrls: Record<string, string> = {
      'bybit': `https://www.bybit.com/en-US/trade/spot/${symbol}/USDT`,
      'binance': `https://www.binance.com/en/trade/${symbol}_USDT`,
      'okx': `https://www.okx.com/trade-spot/${symbol.toLowerCase()}-usdt`
    };
    return exchangeUrls[exchange] || exchangeUrls['bybit'];
  };

  // Don't render if disabled or no data
  if (!settings.enabled || cryptoData.length === 0) {
    return null;
  }

  return (
    <div className="bg-background border-t border-divider py-2 overflow-hidden fixed bottom-0 left-0 right-0 z-50">
      <div 
        className={`flex gap-8 whitespace-nowrap ${
          !isConnected ? 'opacity-60' : ''
        }`}
        onMouseEnter={() => setIsPaused(true)}
        onMouseLeave={() => setIsPaused(false)}
        style={{ 
          animation: `marquee ${getAnimationSpeed()} linear infinite`,
          animationPlayState: isPaused ? 'paused' : 'running',
          willChange: 'transform'
        }}
      >
        {[...cryptoData, ...cryptoData].map((crypto, index) => (
          <a 
            href={crypto.exchangeUrl}
            key={`${crypto.symbol}-${index}`}
            className="flex items-center gap-2 px-4 py-1 cursor-pointer hover:bg-content1 rounded-lg transition-colors"
            onClick={(e) => handleExchangeClick(crypto.exchangeUrl || '', e)}
          >
            <Icon icon={crypto.icon || 'mdi:currency-btc'} width={20} height={20} />
            <span className="text-sm font-medium">{crypto.symbol}</span>
            <span className="font-medium">
              ${crypto.price > 0 ? crypto.price.toLocaleString() : '---'}
            </span>
            {settings.showChangePercent && (
              <span className={`${
                crypto.change24h >= 0 ? "text-success" : "text-danger"
              } font-medium text-sm`}>
                {crypto.change24h >= 0 ? "+" : ""}{crypto.change24h.toFixed(2)}%
              </span>
            )}
            {!isConnected && (
              <Icon 
                icon="mdi:wifi-off" 
                width={12} 
                height={12} 
                className="text-warning opacity-50" 
              />
            )}
          </a>
        ))}
      </div>
    </div>
  );
};

export default CryptoTicker; 
