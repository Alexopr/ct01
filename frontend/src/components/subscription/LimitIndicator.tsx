import React from 'react';
import { Progress, Chip, Tooltip } from '@nextui-org/react';
import type { UsageLimitDto } from '../../types/api';

interface LimitIndicatorProps {
  limit: UsageLimitDto;
  showLabel?: boolean;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

export const LimitIndicator: React.FC<LimitIndicatorProps> = ({ 
  limit, 
  showLabel = true, 
  size = 'md',
  className = ''
}) => {
  const isUnlimited = limit.limit === -1;
  const percentage = isUnlimited ? 0 : Math.min((limit.used / limit.limit) * 100, 100);
  
  const getProgressColor = (percentage: number) => {
    if (percentage >= 90) return 'danger';
    if (percentage >= 70) return 'warning';
    return 'success';
  };

  const getStatusChip = () => {
    if (isUnlimited) {
      return (
        <Chip size="sm" color="primary" variant="flat">
          Безлимит
        </Chip>
      );
    }

    if (percentage >= 100) {
      return (
        <Chip size="sm" color="danger" variant="flat">
          Лимит исчерпан
        </Chip>
      );
    }

    if (percentage >= 90) {
      return (
        <Chip size="sm" color="warning" variant="flat">
          Почти исчерпан
        </Chip>
      );
    }

    return null;
  };

  const formatResetPeriod = (period: string) => {
    switch (period) {
      case 'DAILY':
        return 'ежедневно';
      case 'WEEKLY':
        return 'еженедельно';
      case 'MONTHLY':
        return 'ежемесячно';
      default:
        return period.toLowerCase();
    }
  };

  const formatNextReset = (nextReset: string) => {
    const date = new Date(nextReset);
    const now = new Date();
    const diffHours = Math.ceil((date.getTime() - now.getTime()) / (1000 * 60 * 60));
    
    if (diffHours < 24) {
      return `через ${diffHours} ч.`;
    }
    
    const diffDays = Math.ceil(diffHours / 24);
    return `через ${diffDays} дн.`;
  };

  const progressSize = size === 'sm' ? 'sm' : size === 'lg' ? 'md' : 'sm';

  return (
    <div className={`space-y-2 ${className}`}>
      {showLabel && (
        <div className="flex justify-between items-center">
          <span className={`font-medium ${size === 'sm' ? 'text-sm' : 'text-md'}`}>
            {limit.resourceType}
          </span>
          <div className="flex items-center gap-2">
            {getStatusChip()}
            <span className={`text-default-600 ${size === 'sm' ? 'text-xs' : 'text-sm'}`}>
              {isUnlimited 
                ? `${limit.used} / ∞` 
                : `${limit.used} / ${limit.limit}`
              }
            </span>
          </div>
        </div>
      )}
      
      {!isUnlimited && (
        <Tooltip 
          content={
            <div className="p-2">
              <p className="font-medium">{limit.resourceType}</p>
              <p className="text-sm">Использовано: {limit.used} из {limit.limit}</p>
              <p className="text-sm">Обновление: {formatResetPeriod(limit.resetPeriod)}</p>
              <p className="text-sm">Следующее обновление: {formatNextReset(limit.nextReset)}</p>
            </div>
          }
        >
          <div>
            <Progress 
              value={percentage}
              color={getProgressColor(percentage)}
              size={progressSize}
              className="w-full cursor-help"
              showValueLabel={!showLabel}
              formatOptions={{
                style: 'percent',
                maximumFractionDigits: 1
              }}
            />
          </div>
        </Tooltip>
      )}
      
      {!showLabel && isUnlimited && (
        <div className="text-center">
          <Chip size="sm" color="primary" variant="flat">
            Безлимит
          </Chip>
        </div>
      )}
    </div>
  );
}; 