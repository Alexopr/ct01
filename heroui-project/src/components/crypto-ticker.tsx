import React from "react";
import { Link } from "react-router-dom";
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
  
  // Sample crypto data
  const cryptoData: CryptoData[] = [
    {
      id: "bitcoin",
      name: "Bitcoin",
      symbol: "BTC",
      price: 48235.12,
      change: 5.2,
      icon: "logos:bitcoin",
      exchangeUrl: "https://www.binance.com/en/trade/BTC_USDT"
    },
    {
      id: "ethereum",
      name: "Ethereum",
      symbol: "ETH",
      price: 3421.87,
      change: 3.7,
      icon: "logos:ethereum",
      exchangeUrl: "https://www.binance.com/en/trade/ETH_USDT"
    },
    {
      id: "solana",
      name: "Solana",
      symbol: "SOL",
      price: 98.45,
      change: -1.2,
      icon: "logos:solana",
      exchangeUrl: "https://www.binance.com/en/trade/SOL_USDT"
    },
    {
      id: "cardano",
      name: "Cardano",
      symbol: "ADA",
      price: 0.58,
      change: 2.1,
      icon: "logos:cardano",
      exchangeUrl: "https://www.binance.com/en/trade/ADA_USDT"
    },
    {
      id: "polkadot",
      name: "Polkadot",
      symbol: "DOT",
      price: 7.23,
      change: -0.8,
      icon: "logos:polkadot",
      exchangeUrl: "https://www.binance.com/en/trade/DOT_USDT"
    }
  ];

  const handleExchangeClick = (url: string, e: React.MouseEvent) => {
    e.preventDefault();
    window.open(url, '_blank');
  };

  return (
    <div 
      className="bg-background border-t border-divider py-2 overflow-hidden"
      onMouseEnter={() => setIsPaused(true)}
      onMouseLeave={() => setIsPaused(false)}
    >
      <div 
        className={`flex gap-8 whitespace-nowrap ${isPaused ? '' : 'animate-marquee'}`}
        style={{ animationPlayState: isPaused ? 'paused' : 'running' }}
      >
        {[...cryptoData, ...cryptoData].map((crypto, index) => (
          <a 
            href={crypto.exchangeUrl}
            key={`${crypto.id}-${index}`}
            className="flex items-center gap-2 px-4 py-1 cursor-pointer"
            onClick={(e) => handleExchangeClick(crypto.exchangeUrl, e)}
          >
            <span className="font-medium">${crypto.price.toLocaleString()}</span>
            <span className={`${crypto.change >= 0 ? "text-success" : "text-danger"} font-medium`}>
              {crypto.change >= 0 ? "+" : ""}{crypto.change}%
            </span>
          </a>
        ))}
      </div>
    </div>
  );
};

export default CryptoTicker;