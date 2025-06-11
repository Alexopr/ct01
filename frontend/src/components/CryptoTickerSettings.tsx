import React, { useState, useEffect } from "react";
import { Alert } from "./ui";
import { Select, SelectItem, Switch, Chip, Button, Input } from "@nextui-org/react";
import { Icon } from '@iconify/react';
import { apiService } from '../services/api';
import type { CryptoTickerSettings as TickerSettings, Coin } from '../types/api';

const CryptoTickerSettings: React.FC = () => {
  const [settings, setSettings] = useState<TickerSettings>({
    enabled: true,
    selectedCoins: ['BTC', 'ETH', 'SOL'], // Дефолтные монеты
    exchange: 'bybit',
    animationSpeed: 'medium',
    showChangePercent: true,
  });

  const [availableCoins, setAvailableCoins] = useState<Coin[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loadingCoins, setLoadingCoins] = useState(true);

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

  // Load available coins from API
  useEffect(() => {
    const loadAvailableCoins = async () => {
      try {
        setLoadingCoins(true);
        const coins = await apiService.getActiveCoins();
        setAvailableCoins(coins);
      } catch (error) {
        console.error('Error loading available coins:', error);
        // Fallback to predefined list
        setAvailableCoins([
          { id: 1, symbol: 'BTC', name: 'Bitcoin', isActive: true } as Coin,
          { id: 2, symbol: 'ETH', name: 'Ethereum', isActive: true } as Coin,
          { id: 3, symbol: 'SOL', name: 'Solana', isActive: true } as Coin,
          { id: 4, symbol: 'ADA', name: 'Cardano', isActive: true } as Coin,
          { id: 5, symbol: 'DOT', name: 'Polkadot', isActive: true } as Coin,
        ]);
      } finally {
        setLoadingCoins(false);
      }
    };

    loadAvailableCoins();
  }, []);

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
      // Save settings via API
      await apiService.updateSettings('crypto-ticker', settings);
      
      // Also save in localStorage for immediate frontend usage
      localStorage.setItem('cryptoTickerSettings', JSON.stringify(settings));
      
      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
    } catch (err: any) {
      setError(err.message || 'Ошибка сохранения настроек');
      
      // Fallback to localStorage
      localStorage.setItem('cryptoTickerSettings', JSON.stringify(settings));
      console.warn('Saved to localStorage as fallback');
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

  // Load settings from API and localStorage
  useEffect(() => {
    const loadSettings = async () => {
      try {
        // Try to load from API first
        const apiSettings = await apiService.getSettingsByCategory('crypto-ticker');
        if (apiSettings) {
          // Validate and merge with defaults to ensure all required fields are present
          const validatedSettings: TickerSettings = {
            enabled: apiSettings.enabled ?? true,
            selectedCoins: Array.isArray(apiSettings.selectedCoins) ? apiSettings.selectedCoins : ['BTC', 'ETH', 'SOL'],
            exchange: apiSettings.exchange ?? 'bybit',
            animationSpeed: apiSettings.animationSpeed ?? 'medium',
            showChangePercent: apiSettings.showChangePercent ?? true,
          };
          setSettings(validatedSettings);
          return;
        }
      } catch (error) {
        console.warn('Failed to load settings from API, falling back to localStorage');
      }

      // Fallback to localStorage
      const savedSettings = localStorage.getItem('cryptoTickerSettings');
      if (savedSettings) {
        try {
          const parsed = JSON.parse(savedSettings);
          // Validate parsed settings and merge with defaults
          const validatedSettings: TickerSettings = {
            enabled: parsed.enabled ?? true,
            selectedCoins: Array.isArray(parsed.selectedCoins) ? parsed.selectedCoins : ['BTC', 'ETH', 'SOL'],
            exchange: parsed.exchange ?? 'bybit',
            animationSpeed: parsed.animationSpeed ?? 'medium',
            showChangePercent: parsed.showChangePercent ?? true,
          };
          setSettings(validatedSettings);
        } catch (err) {
          console.error('Ошибка загрузки настроек тикера:', err);
        }
      }
    };

    loadSettings();
  }, []);

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

      {/* Success/Error Alerts */}
      {success && (
        <Alert
          type="success"
          title="Настройки сохранены"
          description="Настройки криптотикера успешно обновлены"
        />
      )}

      {error && (
        <Alert
          type="error"
          title="Ошибка"
          description={error}
        />
      )}

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

      {/* Выбор монет */}
      <div className="space-y-4">
        <div className="space-y-2">
          <h4 className="text-lg font-semibold text-foreground">Выбранные монеты</h4>
          <p className="text-sm text-foreground-500">
            Выберите до 10 монет для отслеживания ({settings.selectedCoins.length}/10)
          </p>
        </div>

        {/* Выбранные монеты */}
        {settings.selectedCoins.length > 0 && (
          <div className="flex flex-wrap gap-2">
            {settings.selectedCoins.map((symbol) => {
              const coin = availableCoins.find(c => c.symbol === symbol);
              return (
                <Chip
                  key={symbol}
                  onClose={() => handleRemoveCoin(symbol)}
                  variant="flat"
                  color="primary"
                  startContent={<Icon icon={getCoinIcon(symbol)} className="w-4 h-4" />}
                >
                  {symbol} {coin && `(${coin.name})`}
                </Chip>
              );
            })}
          </div>
        )}

        {/* Поиск монет */}
        <Input
          placeholder="Поиск монет..."
          value={searchQuery}
          onValueChange={setSearchQuery}
          startContent={<Icon icon="solar:magnifer-bold" className="w-4 h-4" />}
          variant="bordered"
          disabled={loadingCoins}
        />

        {/* Доступные монеты */}
        {loadingCoins ? (
          <div className="flex justify-center py-4">
            <div className="flex items-center gap-2">
              <Icon icon="solar:refresh-circle-bold" className="w-4 h-4 animate-spin" />
              <span className="text-sm text-foreground-500">Загрузка монет...</span>
            </div>
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-2 max-h-64 overflow-y-auto">
            {filteredCoins.map((coin) => (
              <Button
                key={coin.id}
                variant={settings.selectedCoins.includes(coin.symbol) ? "solid" : "bordered"}
                color={settings.selectedCoins.includes(coin.symbol) ? "primary" : "default"}
                size="sm"
                startContent={<Icon icon={getCoinIcon(coin.symbol)} className="w-4 h-4" />}
                onPress={() => handleAddCoin(coin.symbol)}
                disabled={settings.selectedCoins.includes(coin.symbol) || settings.selectedCoins.length >= 10}
                className="justify-start"
              >
                <div className="flex flex-col items-start">
                  <span className="text-xs font-medium">{coin.symbol}</span>
                  <span className="text-xs text-foreground-500 truncate max-w-20">{coin.name}</span>
                </div>
              </Button>
            ))}
          </div>
        )}
      </div>

      {/* Действия */}
      <div className="flex gap-3 pt-4">
        <Button
          color="primary"
          onPress={handleSaveSettings}
          disabled={loading}
          startContent={loading ? 
            <Icon icon="solar:refresh-circle-bold" className="w-4 h-4 animate-spin" /> :
            <Icon icon="solar:diskette-bold" className="w-4 h-4" />
          }
        >
          {loading ? 'Сохранение...' : 'Сохранить настройки'}
        </Button>

        <Button
          variant="bordered"
          onPress={resetToDefaults}
          startContent={<Icon icon="solar:refresh-bold" className="w-4 h-4" />}
        >
          Сброс к умолчанию
        </Button>
      </div>
    </div>
  );
};

export default CryptoTickerSettings; 
