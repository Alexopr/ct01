import React, { useState, useEffect } from "react";
import { Alert } from "./ui";
import { Select, SelectItem, Switch, Chip, Button, Input } from "@nextui-org/react";
import { Icon } from '@iconify/react';

interface CryptoCoin {
  symbol: string;
  name: string;
  icon: string;
  exchangeUrl: string;
}

interface TickerSettings {
  enabled: boolean;
  selectedCoins: string[];
  exchange: string;
  animationSpeed: string;
  showChangePercent: boolean;
}

const CryptoTickerSettings: React.FC = () => {
  const [settings, setSettings] = useState<TickerSettings>({
    enabled: true,
    selectedCoins: ['BTC', 'ETH', 'SOL'], // Дефолтные монеты
    exchange: 'bybit',
    animationSpeed: 'medium',
    showChangePercent: true,
  });

  const availableCoins: CryptoCoin[] = [
    { symbol: 'BTC', name: 'Bitcoin', icon: 'logos:bitcoin', exchangeUrl: 'https://www.bybit.com/en-US/trade/spot/BTC/USDT' },
    { symbol: 'ETH', name: 'Ethereum', icon: 'logos:ethereum', exchangeUrl: 'https://www.bybit.com/en-US/trade/spot/ETH/USDT' },
    { symbol: 'SOL', name: 'Solana', icon: 'logos:solana', exchangeUrl: 'https://www.bybit.com/en-US/trade/spot/SOL/USDT' },
    { symbol: 'ADA', name: 'Cardano', icon: 'logos:cardano', exchangeUrl: 'https://www.bybit.com/en-US/trade/spot/ADA/USDT' },
    { symbol: 'DOT', name: 'Polkadot', icon: 'logos:polkadot', exchangeUrl: 'https://www.bybit.com/en-US/trade/spot/DOT/USDT' },
    { symbol: 'AVAX', name: 'Avalanche', icon: 'logos:avalanche', exchangeUrl: 'https://www.bybit.com/en-US/trade/spot/AVAX/USDT' },
    { symbol: 'MATIC', name: 'Polygon', icon: 'logos:polygon', exchangeUrl: 'https://www.bybit.com/en-US/trade/spot/MATIC/USDT' },
    { symbol: 'LINK', name: 'Chainlink', icon: 'logos:chainlink', exchangeUrl: 'https://www.bybit.com/en-US/trade/spot/LINK/USDT' },
    { symbol: 'UNI', name: 'Uniswap', icon: 'logos:uniswap', exchangeUrl: 'https://www.bybit.com/en-US/trade/spot/UNI/USDT' },
    { symbol: 'ATOM', name: 'Cosmos', icon: 'logos:cosmos', exchangeUrl: 'https://www.bybit.com/en-US/trade/spot/ATOM/USDT' },
    { symbol: 'LTC', name: 'Litecoin', icon: 'logos:litecoin', exchangeUrl: 'https://www.bybit.com/en-US/trade/spot/LTC/USDT' },
    { symbol: 'BCH', name: 'Bitcoin Cash', icon: 'logos:bitcoin-cash', exchangeUrl: 'https://www.bybit.com/en-US/trade/spot/BCH/USDT' },
  ];

  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const exchanges = [
    { key: 'bybit', label: 'Bybit', icon: 'simple-icons:bybit' },
    { key: 'binance', label: 'Binance', icon: 'simple-icons:binance' },
    { key: 'okx', label: 'OKX', icon: 'simple-icons:okx' },
  ];

  const animationSpeeds = [
    { key: 'slow', label: 'Медленно' },
    { key: 'medium', label: 'Нормально' },
    { key: 'fast', label: 'Быстро' },
  ];

  const filteredCoins = availableCoins.filter(coin =>
    coin.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    coin.symbol.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleSettingChange = (key: keyof TickerSettings, value: any) => {
    setSettings(prev => ({ ...prev, [key]: value }));
  };

  const handleAddCoin = (coinSymbol: string) => {
    if (settings.selectedCoins.length >= 10) {
      setError('Максимум 10 монет можно выбрать');
      setTimeout(() => setError(null), 3000);
      return;
    }

    if (!settings.selectedCoins.includes(coinSymbol)) {
      setSettings(prev => ({
        ...prev,
        selectedCoins: [...prev.selectedCoins, coinSymbol]
      }));
    }
  };

  const handleRemoveCoin = (coinSymbol: string) => {
    setSettings(prev => ({
      ...prev,
      selectedCoins: prev.selectedCoins.filter(symbol => symbol !== coinSymbol)
    }));
  };

  const handleSaveSettings = async () => {
    setLoading(true);
    setError(null);

    try {
      // TODO: Реализовать API вызов для сохранения настроек
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // Сохранить в localStorage для демонстрации
      localStorage.setItem('cryptoTickerSettings', JSON.stringify(settings));
      
      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
    } catch (err: any) {
      setError(err.message || 'Ошибка сохранения настроек');
    } finally {
      setLoading(false);
    }
  };

  const resetToDefaults = () => {
    setSettings({
      enabled: true,
      selectedCoins: ['BTC', 'ETH', 'SOL'],
      exchange: 'bybit',
      animationSpeed: 'medium',
      showChangePercent: true,
    });
  };

  // Загружаем настройки из localStorage при монтировании
  useEffect(() => {
    const savedSettings = localStorage.getItem('cryptoTickerSettings');
    if (savedSettings) {
      try {
        const parsed = JSON.parse(savedSettings);
        setSettings(parsed);
      } catch (err) {
        console.error('Ошибка загрузки настроек тикера:', err);
      }
    }
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3 mb-6">
        <div className="p-2 bg-gradient-to-r from-success/20 to-primary/20 rounded-lg">
          <Icon icon="solar:chart-2-bold" className="w-5 h-5 text-success" />
        </div>
        <h3 className="text-xl font-semibold text-foreground">
          Настройки криптотикера
        </h3>
      </div>

      {/* Основные настройки */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div className="space-y-1">
            <h4 className="text-medium font-semibold text-foreground">Включить тикер</h4>
            <p className="text-sm text-foreground-500">Показывать бегущую строку с ценами</p>
          </div>
          <Switch
            isSelected={settings.enabled}
            onValueChange={(value) => handleSettingChange('enabled', value)}
            color="success"
          />
        </div>

        <div className="flex items-center justify-between">
          <div className="space-y-1">
            <h4 className="text-medium font-semibold text-foreground">Показывать изменения</h4>
            <p className="text-sm text-foreground-500">Отображать процент изменения цены</p>
          </div>
          <Switch
            isSelected={settings.showChangePercent}
            onValueChange={(value) => handleSettingChange('showChangePercent', value)}
            color="success"
          />
        </div>

        {/* Выбор биржи и скорости */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Select
            label="Биржа"
            selectedKeys={[settings.exchange]}
            onSelectionChange={(keys) => handleSettingChange('exchange', Array.from(keys)[0])}
            variant="bordered"
            startContent={<Icon icon="solar:buildings-3-bold" className="w-4 h-4" />}
          >
            {exchanges.map((exchange) => (
              <SelectItem key={exchange.key}>
                <div className="flex items-center gap-2">
                  <Icon icon={exchange.icon} className="w-4 h-4" />
                  {exchange.label}
                </div>
              </SelectItem>
            ))}
          </Select>

          <Select
            label="Скорость анимации"
            selectedKeys={[settings.animationSpeed]}
            onSelectionChange={(keys) => handleSettingChange('animationSpeed', Array.from(keys)[0])}
            variant="bordered"
            startContent={<Icon icon="solar:play-bold" className="w-4 h-4" />}
          >
            {animationSpeeds.map((speed) => (
              <SelectItem key={speed.key}>
                {speed.label}
              </SelectItem>
            ))}
          </Select>
        </div>
      </div>

      {/* Выбранные монеты */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h4 className="text-medium font-semibold text-foreground">
            Выбранные монеты ({settings.selectedCoins.length}/10)
          </h4>
          <Button
            size="sm"
            variant="bordered"
            onPress={resetToDefaults}
            startContent={<Icon icon="solar:restart-bold" className="w-4 h-4" />}
          >
            Сбросить
          </Button>
        </div>

        <div className="flex flex-wrap gap-2">
          {settings.selectedCoins.map((symbol) => {
            const coin = availableCoins.find(c => c.symbol === symbol);
            return (
              <Chip
                key={symbol}
                onClose={() => handleRemoveCoin(symbol)}
                variant="flat"
                color="primary"
                startContent={
                  <Icon icon={coin?.icon || 'solar:question-circle-bold'} className="w-4 h-4" />
                }
              >
                {symbol}
              </Chip>
            );
          })}
        </div>
      </div>

      {/* Поиск и добавление монет */}
      <div className="space-y-4">
        <h4 className="text-medium font-semibold text-foreground">Добавить монеты</h4>
        
        <Input
          label="Поиск монет"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          variant="bordered"
          startContent={<Icon icon="solar:magnifer-bold" className="w-4 h-4" />}
          placeholder="Введите название или символ монеты..."
        />

        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-2 max-h-64 overflow-y-auto">
          {filteredCoins.map((coin) => {
            const isSelected = settings.selectedCoins.includes(coin.symbol);
            const canAdd = settings.selectedCoins.length < 10;

            return (
              <Button
                key={coin.symbol}
                size="sm"
                variant={isSelected ? "solid" : "bordered"}
                color={isSelected ? "primary" : "default"}
                disabled={isSelected || !canAdd}
                onPress={() => handleAddCoin(coin.symbol)}
                startContent={
                  <Icon icon={coin.icon} className="w-4 h-4" />
                }
                fullWidth
              >
                <div className="flex flex-col items-start text-left">
                  <span className="font-medium">{coin.symbol}</span>
                  <span className="text-xs opacity-70">{coin.name}</span>
                </div>
              </Button>
            );
          })}
        </div>
      </div>

      {/* Кнопка сохранения */}
      <div className="flex justify-end pt-4">
        <Button
          variant="solid"
          color="primary"
          size="lg"
          disabled={loading}
          onPress={handleSaveSettings}
          startContent={<Icon icon="solar:check-circle-bold" className="w-4 h-4" />}
        >
          {loading ? 'Сохранение...' : 'Сохранить настройки'}
        </Button>
      </div>

      {/* Уведомления */}
      {success && (
        <Alert
          type="success"
          title="Настройки сохранены"
          description="Настройки криптотикера успешно обновлены"
          variant="glass"
        />
      )}
      
      {error && (
        <Alert
          type="error"
          title="Ошибка"
          description={error}
          variant="glass"
        />
      )}
    </div>
  );
};

export default CryptoTickerSettings; 
