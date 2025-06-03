import React, { useState } from "react";
import { Button, Card } from "../components/ui";
import { Icon } from '@iconify/react';
import { getColorClasses, isValidColorVariant } from '../utils/colorUtils';
import type { ColorVariant } from '../utils/colorUtils';

const tools = [
  { 
    id: 1, 
    name: "Price Monitor", 
    desc: "Мониторинг цен и алерты", 
    icon: "solar:graph-up-bold",
    color: "primary" as ColorVariant,
    comingSoon: false
  },
  { 
    id: 2, 
    name: "Trading Bot", 
    desc: "Автоматический торговый бот", 
    icon: "solar:cpu-bolt-bold",
    color: "secondary" as ColorVariant,
    comingSoon: true
  },
  { 
    id: 3, 
    name: "Technical Analysis", 
    desc: "Инструменты технического анализа", 
    icon: "solar:chart-2-bold",
    color: "success" as ColorVariant,
    comingSoon: true
  },
  { 
    id: 4, 
    name: "Portfolio Analyzer", 
    desc: "Анализ и оптимизация портфеля", 
    icon: "solar:pie-chart-bold",
    color: "warning" as ColorVariant,
    comingSoon: true
  },
  { 
    id: 5, 
    name: "Risk Calculator", 
    desc: "Калькулятор рисков и управление капиталом", 
    icon: "solar:shield-warning-bold",
    color: "danger" as ColorVariant,
    comingSoon: true
  },
  { 
    id: 6, 
    name: "Market Screener", 
    desc: "Сканер рынка и поиск возможностей", 
    icon: "solar:magnifer-zoom-in-bold",
    color: "primary" as ColorVariant,
    comingSoon: true
  },
];

const Tools: React.FC = () => {
  const [hoveredTool, setHoveredTool] = useState<number | null>(null);

  const handleToolClick = (tool: typeof tools[0]) => {
    if (tool.comingSoon) {
      return; // Не делаем ничего для инструментов "скоро"
    }
    // TODO: Добавить навигацию к соответствующим инструментам
    console.log(`Opening tool: ${tool.name}`);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background/95 to-background/90 p-6">
      <div className="max-w-7xl mx-auto space-y-8 animate-in fade-in-0 slide-in-from-bottom-4 duration-1000">
        
        {/* Header */}
        <div className="text-center space-y-4">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-r from-primary to-secondary rounded-2xl p-1 animate-in fade-in-0 slide-in-from-top-4 duration-700">
            <div className="w-full h-full bg-background rounded-xl flex items-center justify-center">
              <Icon icon="solar:widget-6-bold" className="w-8 h-8 text-primary" />
            </div>
          </div>
          
          <div className="space-y-2">
            <h1 className="text-4xl md:text-5xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
              Торговые инструменты
            </h1>
            <p className="text-foreground-600 text-lg max-w-2xl mx-auto">
              Профессиональные инструменты для анализа рынка и автоматизации торговли
            </p>
          </div>
        </div>

        {/* Tools Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {tools.map((tool, index) => {
            // Ensure color is valid, fallback to primary if not
            const validColor = isValidColorVariant(tool.color) ? tool.color : 'primary';
            const colorClasses = getColorClasses(validColor);
            
            return (
              <Card
                key={tool.id}
                className={`
                  backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl 
                  transition-all duration-500 hover:shadow-2xl ${colorClasses.gradientBorder}
                  animate-in fade-in-0 slide-in-from-bottom-4
                  ${tool.comingSoon ? 'opacity-75 cursor-not-allowed' : 'cursor-pointer hover:scale-105'}
                `}
                style={{ animationDelay: `${index * 150}ms` }}
                onMouseEnter={() => setHoveredTool(tool.id)}
                onMouseLeave={() => setHoveredTool(null)}
                onClick={() => handleToolClick(tool)}
              >
                <div className="p-6 space-y-4 relative">
                  {/* Coming Soon Badge */}
                  {tool.comingSoon && (
                    <div className="absolute top-3 right-3">
                      <div className="bg-gradient-to-r from-warning/20 to-warning/30 backdrop-blur-sm text-warning text-xs font-semibold px-2 py-1 rounded-full border border-warning/20">
                        Скоро
                      </div>
                    </div>
                  )}

                  {/* Icon */}
                  <div className={`
                    inline-flex items-center justify-center w-16 h-16 
                    bg-gradient-to-r ${colorClasses.iconBg} rounded-2xl backdrop-blur-sm
                    transition-transform duration-300 
                    ${hoveredTool === tool.id && !tool.comingSoon ? 'scale-110' : ''}
                  `}>
                    <Icon 
                      icon={tool.icon} 
                      className={`w-8 h-8 ${colorClasses.iconColor}`}
                    />
                  </div>

                  {/* Content */}
                  <div className="space-y-2">
                    <h3 className="text-xl font-semibold text-foreground">
                      {tool.name}
                    </h3>
                    <p className="text-sm text-foreground-600 leading-relaxed">
                      {tool.desc}
                    </p>
                  </div>

                  {/* Action Button */}
                  <div className="pt-2">
                    {tool.comingSoon ? (
                      <Button
                        variant="ghost"
                        size="md"
                        disabled
                        className="w-full border border-divider/30"
                      >
                        Скоро будет доступно
                      </Button>
                    ) : (
                      <Button
                        variant="solid"
                        size="md"
                        className="w-full transition-all duration-300 hover:shadow-lg bg-primary text-primary-foreground hover:bg-primary/90"
                      >
                        <Icon icon="solar:arrow-right-bold" className="w-4 h-4 mr-2" />
                        Открыть инструмент
                      </Button>
                    )}
                  </div>
                </div>
              </Card>
            );
          })}
        </div>

        {/* Additional Features Section */}
        <div className="pt-8">
          <Card
            className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl animate-in fade-in-0 slide-in-from-bottom-4 duration-700"
            style={{ animationDelay: '900ms' }}
          >
            <div className="p-8 text-center space-y-6">
              <div className="space-y-2">
                <h2 className="text-2xl font-bold text-foreground">
                  Нужен особый инструмент?
                </h2>
                <p className="text-foreground-600 max-w-2xl mx-auto">
                  Мы постоянно работаем над добавлением новых функций. 
                  Предложите свою идею или запросите разработку индивидуального инструмента.
                </p>
              </div>
              
              <div className="flex flex-col sm:flex-row gap-4 justify-center">
                <Button
                  variant="solid"
                  size="lg"
                  className="bg-gradient-to-r from-primary to-secondary text-white transition-all duration-300 hover:shadow-lg"
                >
                  <Icon icon="solar:lightbulb-bolt-bold" className="w-5 h-5 mr-2" />
                  Предложить идею
                </Button>
                <Button
                  variant="bordered"
                  size="lg"
                  className="border-primary text-primary hover:bg-primary/10 transition-all duration-300"
                >
                  <Icon icon="solar:phone-bold" className="w-5 h-5 mr-2" />
                  Связаться с нами
                </Button>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default Tools; 



