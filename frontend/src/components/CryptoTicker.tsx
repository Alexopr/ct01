import React from "react";
import { Icon } from "@iconify/react";

interface CryptoData {
  id: string;
  name: string;
  symbol: string;
  price: number;
  change: number;
  icon: string;
  exchangeUrl: string;
}

const CryptoTicker: React.FC = () => {
  const [isPaused, setIsPaused] = React.useState(false);
  
  // Sample crypto data - will be replaced with Bybit API data
  const cryptoData: CryptoData[] = [
    {
      id: "bitcoin",
      name: "Bitcoin",
      symbol: "BTC",
      price: 48235.12,
      change: 5.2,
      icon: "logos:bitcoin",
      exchangeUrl: "https://www.bybit.com/en-US/trade/spot/BTC/USDT"
    },
    {
      id: "ethereum",
      name: "Ethereum",
      symbol: "ETH",
      price: 3421.87,
      change: 3.7,
      icon: "logos:ethereum",
      exchangeUrl: "https://www.bybit.com/en-US/trade/spot/ETH/USDT"
    },
    {
      id: "solana",
      name: "Solana",
      symbol: "SOL",
      price: 98.45,
      change: -1.2,
      icon: "logos:solana",
      exchangeUrl: "https://www.bybit.com/en-US/trade/spot/SOL/USDT"
    },
    {
      id: "cardano",
      name: "Cardano",
      symbol: "ADA",
      price: 0.58,
      change: 2.1,
      icon: "logos:cardano",
      exchangeUrl: "https://www.bybit.com/en-US/trade/spot/ADA/USDT"
    },
    {
      id: "polkadot",
      name: "Polkadot",
      symbol: "DOT",
      price: 7.23,
      change: -0.8,
      icon: "logos:polkadot",
      exchangeUrl: "https://www.bybit.com/en-US/trade/spot/DOT/USDT"
    }
  ];

  const handleExchangeClick = (url: string, e: React.MouseEvent) => {
    e.preventDefault();
    window.open(url, '_blank');
  };

  return (
    <div 
      className="bg-background border-t border-divider py-2 overflow-hidden fixed bottom-0 left-0 right-0 z-50"
    >
      <div 
        className="flex gap-8 whitespace-nowrap animate-marquee hover:pause-animation"
        onMouseEnter={() => setIsPaused(true)}
        onMouseLeave={() => setIsPaused(false)}
        style={{ 
          animationPlayState: isPaused ? 'paused' : 'running',
          willChange: 'transform'
        }}
      >
        {[...cryptoData, ...cryptoData].map((crypto, index) => (
          <a 
            href={crypto.exchangeUrl}
            key={`${crypto.id}-${index}`}
            className="flex items-center gap-2 px-4 py-1 cursor-pointer hover:bg-content1 rounded-lg transition-colors"
            onClick={(e) => handleExchangeClick(crypto.exchangeUrl, e)}
          >
            <Icon icon={crypto.icon} width={20} height={20} />
            <span className="text-sm font-medium">{crypto.symbol}</span>
            <span className="font-medium">${crypto.price.toLocaleString()}</span>
            <span className={`${crypto.change >= 0 ? "text-success" : "text-danger"} font-medium text-sm`}>
              {crypto.change >= 0 ? "+" : ""}{crypto.change}%
            </span>
          </a>
        ))}
      </div>
    </div>
  );
};

export default CryptoTicker; 
