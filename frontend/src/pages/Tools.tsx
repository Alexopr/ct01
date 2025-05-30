import React, { useState } from "react";
import { Button, Card } from "../components/ui";
import { Icon } from '@iconify/react';
import { useNavigate } from 'react-router-dom';

const tools = [
  { 
    id: 1, 
    name: "Arbitrage Scanner", 
    desc: "Поиск арбитражных возможностей между биржами", 
    icon: "solar:transfer-horizontal-bold",
    color: "primary",
    comingSoon: false
  },
  { 
    id: 2, 
    name: "Order Book Analyzer", 
    desc: "Анализ глубины рынка и стакана заявок", 
    icon: "solar:chart-2-bold",
    color: "secondary",
    comingSoon: false
  },
  { 
    id: 3, 
    name: "Signal Tracker", 
    desc: "Отслеживание и анализ торговых сигналов", 
    icon: "solar:radar-2-bold",
    color: "success",
    comingSoon: false
  },
  { 
    id: 4, 
    name: "Portfolio Analyzer", 
    desc: "Анализ и оптимизация портфеля", 
    icon: "solar:pie-chart-bold",
    color: "warning",
    comingSoon: true
  },
  { 
    id: 5, 
    name: "Risk Calculator", 
    desc: "Калькулятор рисков и управление капиталом", 
    icon: "solar:shield-warning-bold",
    color: "danger",
    comingSoon: true
  },
  { 
    id: 6, 
    name: "Market Screener", 
    desc: "Сканер рынка и поиск возможностей", 
    icon: "solar:magnifer-zoom-in-bold",
    color: "primary",
    comingSoon: true
  },
];

const Tools: React.FC = () => {
  const navigate = useNavigate();
  const [hoveredTool, setHoveredTool] = useState<number | null>(null);

  const handleToolClick = (tool: typeof tools[0]) => {
    if (tool.comingSoon) {
      return; // Не делаем ничего для инструментов "скоро"
    }
    // TODO: Добавить навигацию к соответствующим инструментам
    console.log(`Opening tool: ${tool.name}`);
  };

  const getColorClasses = (color: string) => {
    switch (color) {
      case 'primary':
        return {
          iconBg: 'from-primary/20 to-primary/30',
          iconColor: 'text-primary',
          gradientBorder: 'hover:shadow-primary/20'
        };
      case 'secondary':
        return {
          iconBg: 'from-secondary/20 to-secondary/30',
          iconColor: 'text-secondary',
          gradientBorder: 'hover:shadow-secondary/20'
        };
      case 'success':
        return {
          iconBg: 'from-success/20 to-success/30',
          iconColor: 'text-success',
          gradientBorder: 'hover:shadow-success/20'
        };
      case 'warning':
        return {
          iconBg: 'from-warning/20 to-warning/30',
          iconColor: 'text-warning',
          gradientBorder: 'hover:shadow-warning/20'
        };
      case 'danger':
        return {
          iconBg: 'from-danger/20 to-danger/30',
          iconColor: 'text-danger',
          gradientBorder: 'hover:shadow-danger/20'
        };
      default:
        return {
          iconBg: 'from-primary/20 to-primary/30',
          iconColor: 'text-primary',
          gradientBorder: 'hover:shadow-primary/20'
        };
    }
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
            const colorClasses = getColorClasses(tool.color);
            
            return (
              <Card
                key={tool.id}
                variant="glass"
                hoverable={!tool.comingSoon}
                className={`
                  backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl 
                  transition-all duration-500 hover:shadow-2xl ${colorClasses.gradientBorder}
                  animate-in fade-in-0 slide-in-from-bottom-4
                  ${tool.comingSoon ? 'opacity-75 cursor-not-allowed' : 'cursor-pointer'}
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
                        variant={tool.color as any}
                        size="md"
                        gradient={tool.color === 'primary' || tool.color === 'secondary'}
                        icon="solar:arrow-right-bold"
                        className="w-full transition-all duration-300 hover:shadow-lg"
                      >
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
            variant="glass"
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
                  variant="primary"
                  size="lg"
                  gradient
                  icon="solar:lightbulb-bolt-bold"
                  className="px-8"
                >
                  Предложить идею
                </Button>
                <Button
                  variant="ghost"
                  size="lg"
                  icon="solar:chat-round-call-bold"
                  className="px-8 border border-divider/30 hover:border-primary/50"
                >
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