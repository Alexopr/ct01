import React, { useState } from "react";
import { Card, Button, Input } from "../components/ui";
import { Link } from "@nextui-org/react";
import { Icon } from '@iconify/react';

// Список бирж с дополнительной информацией
const exchanges = [
  { 
    name: "Binance", 
    url: "https://www.binance.com/", 
    logo: "/exchanges/binance.svg",
    description: "Крупнейшая в мире криптобиржа",
    rating: 4.8,
    volume: "$18.2B",
    established: "2017",
    category: "spot"
  },
  { 
    name: "OKX", 
    url: "https://www.okx.com/", 
    logo: "/exchanges/okx.svg",
    description: "Лидирующая глобальная криптобиржа",
    rating: 4.6,
    volume: "$12.1B",
    established: "2017",
    category: "derivatives"
  },
  { 
    name: "BYBIT", 
    url: "https://www.bybit.com/", 
    logo: "/exchanges/bybit.svg",
    description: "Специализируется на деривативах",
    rating: 4.7,
    volume: "$8.5B",
    established: "2018",
    category: "derivatives"
  },
  { 
    name: "MEXC Global", 
    url: "https://www.mexc.com/", 
    logo: "/exchanges/mexc.svg",
    description: "Широкий выбор альткоинов",
    rating: 4.3,
    volume: "$3.2B",
    established: "2018",
    category: "spot"
  },
  { 
    name: "Gate.io", 
    url: "https://www.gate.io/", 
    logo: "/exchanges/gate.svg",
    description: "Одна из старейших криптобирж",
    rating: 4.4,
    volume: "$2.8B",
    established: "2013",
    category: "spot"
  },
  { 
    name: "KuCoin", 
    url: "https://www.kucoin.com/", 
    logo: "/exchanges/kucoin.svg",
    description: "Народная биржа с токеном KCS",
    rating: 4.5,
    volume: "$2.1B",
    established: "2017",
    category: "spot"
  },
  { 
    name: "Bitget", 
    url: "https://www.bitget.com/", 
    logo: "/exchanges/bitget.svg",
    description: "Копи-трейдинг и фьючерсы",
    rating: 4.2,
    volume: "$1.8B",
    established: "2018",
    category: "derivatives"
  },
  { 
    name: "Huobi", 
    url: "https://www.huobi.com/", 
    logo: "/exchanges/huobi.svg",
    description: "Глобальная технологическая компания",
    rating: 4.1,
    volume: "$1.5B",
    established: "2013",
    category: "spot"
  },
  { 
    name: "Coinbase", 
    url: "https://www.coinbase.com/", 
    logo: "/exchanges/coinbase.svg",
    description: "Публичная компания, США",
    rating: 4.3,
    volume: "$1.2B",
    established: "2012",
    category: "spot"
  },
  { 
    name: "Kraken", 
    url: "https://www.kraken.com/", 
    logo: "/exchanges/kraken.svg",
    description: "Надежная биржа с высокой безопасностью",
    rating: 4.4,
    volume: "$800M",
    established: "2011",
    category: "spot"
  },
  { 
    name: "Crypto.com", 
    url: "https://crypto.com/", 
    logo: "/exchanges/crypto.svg",
    description: "Экосистема криптоуслуг",
    rating: 4.0,
    volume: "$700M",
    established: "2016",
    category: "spot"
  },
  { 
    name: "Bitfinex", 
    url: "https://www.bitfinex.com/", 
    logo: "/exchanges/bitfinex.svg",
    description: "Продвинутая торговая платформа",
    rating: 3.9,
    volume: "$500M",
    established: "2012",
    category: "spot"
  },
];

const categories = [
  { key: 'all', label: 'Все биржи', icon: 'solar:widget-6-bold' },
  { key: 'spot', label: 'Спот торговля', icon: 'solar:chart-2-bold' },
  { key: 'derivatives', label: 'Деривативы', icon: 'solar:graph-up-bold' },
];

const Exchanges: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("all");

  const filteredExchanges = exchanges.filter(exchange => {
    const matchesSearch = exchange.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         exchange.description.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === 'all' || exchange.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  const getRatingColor = (rating: number) => {
    if (rating >= 4.5) return 'text-success';
    if (rating >= 4.0) return 'text-warning';
    return 'text-danger';
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background/95 to-background/90 p-6">
      <div className="max-w-7xl mx-auto space-y-8 animate-in fade-in-0 slide-in-from-bottom-4 duration-1000">
        
        {/* Header */}
        <div className="text-center space-y-4">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-r from-primary to-secondary rounded-2xl p-1 animate-in fade-in-0 slide-in-from-top-4 duration-700">
            <div className="w-full h-full bg-background rounded-xl flex items-center justify-center">
              <Icon icon="solar:buildings-3-bold" className="w-8 h-8 text-primary" />
            </div>
          </div>
          
          <div className="space-y-2">
            <h1 className="text-4xl md:text-5xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
              Криптобиржи
            </h1>
            <p className="text-foreground-600 text-lg max-w-2xl mx-auto">
              Обзор ведущих криптовалютных бирж с актуальной информацией о торговых объемах и рейтингах
            </p>
          </div>
        </div>

        {/* Filters */}
        <div className="flex flex-col md:flex-row gap-4 items-center justify-between">
          {/* Search */}
          <div className="w-full md:w-96">
            <Input
              placeholder="Поиск биржи..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              variant="bordered"
              startContent={<Icon icon="solar:magnifer-zoom-in-bold" className="w-4 h-4" />}
              className="w-full"
            />
          </div>

          {/* Category Filter */}
          <div className="flex gap-2">
            {categories.map((category) => (
              <Button
                key={category.key}
                variant={selectedCategory === category.key ? "solid" : "ghost"}
                color={selectedCategory === category.key ? "primary" : "default"}
                size="md"
                startContent={<Icon icon={category.icon} className="w-4 h-4" />}
                onClick={() => setSelectedCategory(category.key)}
                className={`transition-all duration-300 ${
                  selectedCategory === category.key 
                    ? 'shadow-lg' 
                    : 'border border-divider/30 hover:border-primary/50'
                }`}
              >
                {category.label}
              </Button>
            ))}
          </div>
        </div>

        {/* Results Count */}
        <div className="text-sm text-foreground-500">
          Найдено {filteredExchanges.length} из {exchanges.length} бирж
        </div>

        {/* Exchanges Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {filteredExchanges.map((exchange, index) => (
            <Card
              key={exchange.name} className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl transition-all duration-500 hover:shadow-2xl hover:shadow-primary/10 animate-in fade-in-0 slide-in-from-bottom-4"
              style={{ animationDelay: `${index * 100}ms` }}
            >
              <div className="p-6 space-y-4">
                {/* Logo and Rating */}
                <div className="flex items-start justify-between">
                  <div className="flex items-center justify-center w-12 h-12 bg-white rounded-lg p-2">
                    <img 
                      src={exchange.logo} 
                      alt={exchange.name}
                      className="max-w-full max-h-full object-contain"
                      style={{ filter: 'none' }}
                    />
                  </div>
                  
                  <div className="flex items-center gap-1">
                    <Icon icon="solar:star-bold" className={`w-4 h-4 ${getRatingColor(exchange.rating)}`} />
                    <span className={`text-sm font-semibold ${getRatingColor(exchange.rating)}`}>
                      {exchange.rating}
                    </span>
                  </div>
                </div>

                {/* Exchange Info */}
                <div className="space-y-2">
                  <h3 className="text-xl font-bold text-foreground">
                    {exchange.name}
                  </h3>
                  <p className="text-sm text-foreground-600 line-clamp-2">
                    {exchange.description}
                  </p>
                </div>

                {/* Stats */}
                <div className="grid grid-cols-2 gap-3 text-xs">
                  <div className="bg-background/50 rounded-lg p-2 text-center">
                    <div className="text-foreground font-semibold">{exchange.volume}</div>
                    <div className="text-foreground-500">Объем 24ч</div>
                  </div>
                  <div className="bg-background/50 rounded-lg p-2 text-center">
                    <div className="text-foreground font-semibold">{exchange.established}</div>
                    <div className="text-foreground-500">Год основания</div>
                  </div>
                </div>

                {/* Category Badge */}
                <div className="flex items-center justify-between">
                  <div className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium ${
                    exchange.category === 'spot' 
                      ? 'bg-primary/20 text-primary' 
                      : 'bg-secondary/20 text-secondary'
                  }`}>
                    <Icon 
                      icon={exchange.category === 'spot' ? 'solar:chart-2-bold' : 'solar:graph-up-bold'} 
                      className="w-3 h-3" 
                    />
                    {exchange.category === 'spot' ? 'Спот' : 'Деривативы'}
                  </div>
                </div>

                {/* Action Button */}
                <Link 
                  href={exchange.url} 
                  target="_blank" 
                  rel="noopener noreferrer"
                  className="block"
                >
                  <Button
                    color="primary"
                    size="md" startContent={<Icon icon="solar:export-bold" className="w-4 h-4" />}
                    className="w-full transition-all duration-300 hover:shadow-lg"
                  >
                    Перейти на биржу
                  </Button>
                </Link>
              </div>
            </Card>
          ))}
        </div>

        {/* No Results */}
        {filteredExchanges.length === 0 && (
          <div className="text-center py-12">
            <Icon icon="solar:magnifer-zoom-in-bold" className="w-16 h-16 text-foreground-400 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-foreground mb-2">
              Биржи не найдены
            </h3>
            <p className="text-foreground-600">
              Попробуйте изменить критерии поиска или фильтр категории
            </p>
          </div>
        )}

        {/* Info Section */}
        <div className="pt-8">
          <Card
            
            className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl animate-in fade-in-0 slide-in-from-bottom-4 duration-700"
            style={{ animationDelay: '600ms' }}
          >
            <div className="p-8 text-center space-y-6">
              <div className="space-y-2">
                <h2 className="text-2xl font-bold text-foreground">
                  Важная информация
                </h2>
                <p className="text-foreground-600 max-w-3xl mx-auto">
                  Данные о торговых объемах и рейтингах обновляются регулярно. 
                  Всегда проводите собственное исследование перед началом торговли на любой бирже.
                  Рейтинги основаны на безопасности, ликвидности и удобстве использования.
                </p>
              </div>

              <div className="flex flex-col sm:flex-row gap-4 justify-center">
                <Button
                  color="primary"
                  size="lg" startContent={<Icon icon="solar:info-circle-bold" className="w-4 h-4" />}
                  className="px-8"
                >
                  Руководство по выбору биржи
                </Button>
                <Button
                  variant="ghost"
                  size="lg"
                  startContent={<Icon icon="solar:shield-check-bold" className="w-4 h-4" />}
                  className="px-8 border border-divider/30 hover:border-primary/50"
                >
                  Безопасность торговли
                </Button>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default Exchanges; 



